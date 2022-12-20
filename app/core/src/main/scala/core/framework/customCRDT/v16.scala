package core.framework.customCRDT.v16

import core.framework.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

// A claim is something that can be required by a event. To be able to create an event, you must prove that you have the required claims.
case class Claim[I](
  val publicKey: Array[Byte],
  val id: I,
)

object Claim:
  def create[C](claimIds: Set[C])(using crypt: Crypt): Future[(List[Claim[C]], List[ClaimProver[C]])] =
    Future.sequence(
        claimIds.map(create(_)).toList
      )
      .map(_.unzip)

  def create[C](claimId: C)(using crypt: Crypt): Future[(Claim[C], ClaimProver[C])] =
    for
      CryptKeyValuePair(privateKey, publicKey) <- crypt.generateKey
    yield
      (
        Claim(publicKey, claimId),
        ClaimProver(privateKey, claimId)
      )

// A claim proof is a proof that you have a claim. It is only a proof for your replicaId.
case class ClaimProof[C](
  val proof: Array[Byte],
  val id: C,
):
  // Verifies that proof is the replicaID encrypted with the private key of the claim.
  def verify(claim: Claim[C], replicaId: String)(using crypt: Crypt): Future[Boolean] =
    crypt.verify(claim.publicKey, replicaId, proof)

// A claim prover allows you to prove that any replicaId has a claim.
case class ClaimProver[ID](
  val privateKey: Array[Byte],
  val id: ID
):
  // Encrypts the replicaId with the private key of the claim.
  def prove(replicaId: String)(using crypt: Crypt): Future[ClaimProof[ID]] =
    for
      proof <- crypt.sign(privateKey, replicaId)
    yield
      ClaimProof(proof, id)

trait ClaimRegistry[I]:
  def proof(claim: I): Future[ClaimProof[I]]

trait Event[A, C]:
  def asEffect: Effect[A, C]

case class EventWithContext[A, C](
  val event: Event[A, C],
  val context: C
)

case class Effect[A, C](
  val verify:  (A, C) => Future[Boolean],
  val advance: (A, C) => Future[A]
)

trait EffectExtender[A, B, C]:
  def extend(inner: Effect[A, C]): Effect[B, C]

object EffectExtender:
  def apply[A, B, C](using extender: EffectExtender[A, B, C]): EffectExtender[A, B, C] =
    extender

  given transitive[X, Y, Z, C](using le: EffectExtender[X, Y, C], re: EffectExtender[Y, Z, C]): EffectExtender[X, Z, C] with
    def extend(effect: Effect[X, C]): Effect[Z, C] =
      re.extend(le.extend(effect))

  given identity[A, C]: EffectExtender[A, A, C] with
    def extend(effect: Effect[A, C]): Effect[A, C] =
      effect

trait IdentityContext:
  val replicaId: String

trait AsymPermissionContext[I]:
  val proofs: Set[ClaimProof[I]]
  def claims = proofs.map(_.id)

case class AsymPermissionStateExtension[A, I](
  val inner: A,
  val claims: Set[Claim[I]]
)

object AsymPermissionStateExtension:
  given [A, I, C <: AsymPermissionContext[I] with IdentityContext](using Crypt): EffectExtender[A, AsymPermissionStateExtension[A, I], C] with
    def extend(inner: Effect[A, C]): Effect[AsymPermissionStateExtension[A, I], C] =
      Effect(
        (state, context) => 
          // Ensure that all proofs in context are valid.
          for
            coreValid <- Future.sequence(
              for
                proof <- context.proofs
                claim <- state.claims.find(_.id == proof.id)
              yield
                proof.verify(claim, context.replicaId)
            )

            valid <- if coreValid.forall(identity) then 
              inner.verify(state.inner, context) 
            else 
              Future.successful(false)

          yield
            valid,
        (state, context) =>
          for
            inner <- inner.advance(state.inner, context)
          yield
            state.copy(inner = inner)
      )

enum CounterRoles:
  case Adder

case class Counter(
  val value: Int
)

type CounterWithExtensions = AsymPermissionStateExtension[Counter, CounterRoles]

case class CounterContext(
  val replicaId: String,
  val proofs: Set[ClaimProof[CounterRoles]]
) extends IdentityContext with AsymPermissionContext[CounterRoles]

case class AddCounterEvent(
  val value: Int
) extends Event[Counter, CounterContext]:
  def asEffect: Effect[Counter, CounterContext] =
    Effect(
      (state, context) => Future.successful(context.proofs.exists(_.id == CounterRoles.Adder)),
      (state, context) => Future.successful(state.copy(value = state.value + value))
    )

object AddCounterEvent:
  def create(replicaId: String, value: Int)(using registry: ClaimRegistry[CounterRoles]) =
    for
      proof <- registry.proof(CounterRoles.Adder)
    yield
      EventWithContext(
        AddCounterEvent(value),
        CounterContext(replicaId, Set(proof))
      )
    
def main(using Crypt) = 
  val replicaId = "replicaId"

  for
    // Step 1: Create claims and claimProvers.
    (claims, claimProvers) <- Claim.create(Set(CounterRoles.Adder))

    registry = new ClaimRegistry[CounterRoles]:
      def proof(claim: CounterRoles): Future[ClaimProof[CounterRoles]] =
        claimProvers.find(_.id == claim).get.prove(replicaId)

    // Step 2: Create initial state.
    state = AsymPermissionStateExtension(Counter(0), claims.toSet)

    // Step 3: Create event.
    event <- AddCounterEvent.create(replicaId, 5)(using registry)

    // Step 4: Get effect
    effect = event.event.asEffect

    // Step 5: Extend effect
    extendedEffect = EffectExtender[Counter, CounterWithExtensions, CounterContext].extend(effect)

    // Step 6: Verify and advance state.
    valid <- extendedEffect.verify(state, event.context)
    newState <- extendedEffect.advance(state, event.context)

    fakeEvent1 = EventWithContext(
      AddCounterEvent(5),
      CounterContext("fakeReplicaId", event.context.proofs)
    )

    fakeEvent2 = EventWithContext(
      AddCounterEvent(5),
      CounterContext(replicaId, Set.empty)
    )

    fakeEvent3 = EventWithContext(
      AddCounterEvent(4),
      CounterContext(replicaId, Set(ClaimProof("fakeProof".getBytes, CounterRoles.Adder)))
    )

    fakeValid1 <- EffectExtender[Counter, CounterWithExtensions, CounterContext]
      .extend(fakeEvent1.event.asEffect)
      .verify(state, fakeEvent1.context)
    
    fakeValid2 <- EffectExtender[Counter, CounterWithExtensions, CounterContext]
      .extend(fakeEvent2.event.asEffect)
      .verify(state, fakeEvent2.context)

    fakeValid3 <- EffectExtender[Counter, CounterWithExtensions, CounterContext]
      .extend(fakeEvent3.event.asEffect)
      .verify(state, fakeEvent3.context)

  do
    println(s"State is valid: $valid")
    println(s"Fake state 1 is valid: $fakeValid1")
    println(s"Fake state 2 is valid: $fakeValid2")
    println(s"Fake state 3 is valid: $fakeValid3")
    println(s"Old state was: ${state.inner.value}")
    println(s"New state is: ${newState.inner.value}")
