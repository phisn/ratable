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
  def create[C](claim: C)(using crypt: Crypt) =
    for
      CryptKeyValuePair(privateKey, publicKey) <- crypt.generateKey
    yield
      (
        Claim(publicKey, claim),
        ClaimProver(privateKey, claim)
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
            valid <- Future.sequence(
              for
                proof <- context.proofs
                claim <- state.claims.find(_.id == proof.id)
              yield
                proof.verify(claim, context.replicaId)
            )
          yield
            valid.forall(identity),
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

type CounterState = AsymPermissionStateExtension[Counter, CounterRoles]

case class CounterContext(
  val replicaId: String,
  val proofs: Set[ClaimProof[CounterRoles]]
) extends IdentityContext with AsymPermissionContext[CounterRoles]

case class AddCounterEvent(
  val value: Int
) extends Event[CounterState, CounterContext]:
  def asEffect: Effect[CounterState, CounterContext] =
    Effect(
      (state, context) => Future.successful(context.proofs.exists(_.id == CounterRoles.Adder)),
      (state, context) => Future.successful(state.copy(inner = state.inner.copy(value = state.inner.value + value)))
    )

object AddCounterEvent:
  def create(value: Int)(using registry: ClaimRegistry[CounterRoles]): Future[AddCounterEvent] =
    for
      proof <- registry.proof(CounterRoles.Adder)
    yield
      
