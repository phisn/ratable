package core.framework.customCRDT.v17

import core.framework.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

// A claim is something that can be required by a event. To be able to create an event, you must prove that you have the required claims.
case class Claim[I](
  val publicKey: Array[Byte],
  val id: I,
)

object Claim:
  def create[C](claimIds: Set[C])(using crypt: Crypt): Future[(Set[Claim[C]], Set[ClaimProver[C]])] =
    Future.sequence(
        claimIds.map(create(_))
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
  val verify:  (A, C) => Future[Option[String]],
  val advance: (A, C) => Future[A]
)

object Effect:
  def apply[A, C](verify: (A, C) => Future[Option[String]], advance: (A, C) => Future[A]): Effect[A, C] =
    new Effect(verify, advance)

  def from[A, C](verify: (A, C) => Option[String], advance: (A, C) => A): Effect[A, C] =
    new Effect((a, c) => Future.successful(verify(a, c)), (a, c) => Future.successful(advance(a, c)))

trait EffectPipelineStage[A, C]:
  def apply(effect: Effect[A, C]): Effect[A, C]

trait EffectPipeline[A, C]:
  def stages: List[EffectPipelineStage[A, C]]

  def combine: EffectPipelineStage[A, C] = 
    effect => stages.foldLeft(effect)((x, y) => y.apply(x))

case class ECmRDT[A, C](
  val state: A,
  val effectPipeline: EffectPipelineStage[A, C]
):
  def effect(eventWithContext: EventWithContext[A, C]): Future[Either[String, ECmRDT[A, C]]] =
    val effect = effectPipeline(eventWithContext.event.asEffect)

    for
      valid <- effect.verify(state, eventWithContext.context)

      newState <- valid match
        case Some(error) => Future.successful(Left(error))
        case None => effect.advance(state, eventWithContext.context).map(Right(_))
    yield
      newState.map(x => copy(state = x))

object ECmRDT:
  def apply[A, C](state: A)(using pipeline: EffectPipeline[A, C]): ECmRDT[A, C] =
    ECmRDT(state, pipeline.combine)

trait IdentityContext:
  val replicaId: String

trait AsymPermissionContextExtension[I]:
  val proofs: Set[ClaimProof[I]]
  def claims = proofs.map(_.id)
  
trait AsymPermissionStateExtension[I]:
  val claims: Set[Claim[I]]

object AsymPermissionEffectPipeline:
  def apply[A <: AsymPermissionStateExtension[I], I, C <: AsymPermissionContextExtension[I] with IdentityContext](using Crypt): EffectPipelineStage[A, C] =
    (effect: Effect[A, C]) =>
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
              effect.verify(state, context) 
            else
              Future.successful(Some("Invalid proof."))

          yield
            valid.orElse(
              Option.unless(context.proofs.exists(x => state.claims.exists(_.id == x.id)))
                ("Claim does not exist.")
            ),
        (state, context) =>
          for
            inner <- effect.advance(state, context)
          yield
            inner
      )
  
enum CounterRoles:
  case Adder

case class Counter(
  val value: Int,
  val claims: Set[Claim[CounterRoles]]
) extends AsymPermissionStateExtension[CounterRoles]

given (using Crypt): EffectPipeline[Counter, CounterContext] with
  def stages = List(
    AsymPermissionEffectPipeline[Counter, CounterRoles, CounterContext]
  )

case class CounterContext(
  val replicaId: String,
  val proofs: Set[ClaimProof[CounterRoles]]
) extends IdentityContext with AsymPermissionContextExtension[CounterRoles]

case class AddCounterEvent(
  val value: Int
) extends Event[Counter, CounterContext]:
  def asEffect: Effect[Counter, CounterContext] =
    Effect.from(
      (state, context) => Option.unless(context.proofs.exists(_.id == CounterRoles.Adder))("Missing role."),
      (state, context) => state.copy(value = state.value + value)
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
    counter = ECmRDT[Counter, CounterContext](Counter(0, claims))

    // Step 3: Create event.
    event <- AddCounterEvent.create(replicaId, 5)(using registry)

    // Step 4: Verify and advance state.
    newCounter <- counter.effect(event)

    // Create and test fake events to verify that the effect verification is working.
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

    fakeValid1 <- counter.effect(fakeEvent1).map(_.swap.toOption)
    fakeValid2 <- counter.effect(fakeEvent2).map(_.swap.toOption)
    fakeValid3 <- counter.effect(fakeEvent3).map(_.swap.toOption)

  do
    println(s"State is valid: ${newCounter.swap.toOption}")
    println(s"Fake state 1 is valid: $fakeValid1")
    println(s"Fake state 2 is valid: $fakeValid2")
    println(s"Fake state 3 is valid: $fakeValid3")
    println(s"Old state was: ${counter.state.value}")
    println(s"New state is: ${newCounter.map(_.state.value)}")
    println(s"v17")
