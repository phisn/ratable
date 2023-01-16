package core.framework.ecmrdt.example

import cats.data.*
import cats.implicits.*
import core.framework.*
import core.framework.ecmrdt.*
import core.framework.ecmrdt.extensions.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

enum CounterRoles:
  case Adder
/*
case class Counter(
  val value: Int,
  val claims: List[Claim[CounterRoles]]
) 
extends AsymPermissionStateExtension[CounterRoles]

case class CounterContext(
  val replicaId: ReplicaId,
  val proofs: List[ClaimProof[CounterRoles]]
) 
extends IdentityContext 
   with AsymPermissionContextExtension[CounterRoles]

object Counter:
  given (using Crypt): EffectPipeline[Counter, CounterContext] = EffectPipeline(
    AsymPermissionEffectPipeline[Counter, CounterRoles, CounterContext]
  )

sealed trait CounterEvent extends Event[Counter, CounterContext]

case class AddCounterEvent(
  val value: Int
) extends CounterEvent:
  def asEffect: Effect[Counter, CounterContext] =
    (state, context) => 
      for
        _ <- context.verifyClaim(CounterRoles.Adder)
      yield
        state.copy(value = state.value + value)

def addCounterEvent(replicaId: ReplicaId, value: Int)(using registry: ClaimRegistry[CounterRoles]) =
  withProofs(CounterRoles.Adder) { proofs => 
    EventWithContext(
      AddCounterEvent(value),
      CounterContext(replicaId, proofs)
    )
  }
  
*/
/*
def main(using Crypt) = 
  for
    replicaId <- PrivateReplicaId()

    // Step 1: Create claims and claimProvers.
    (claims, claimProvers) <- Claim.create(Set(CounterRoles.Adder))

    registry = new ClaimRegistry[CounterRoles]:
      def proof(claim: CounterRoles): Future[ClaimProof[CounterRoles]] =
        claimProvers.find(_.id == claim).get.prove(replicaId)

    // Step 2: Create initial state.
    counter = ECmRDT[Counter, CounterContext, CounterEvent](Counter(0, claims))

    // Step 3: Create event.
    event <- addCounterEvent(replicaId, 5)(using registry)

    eventPrepared <- counter.prepare(event)

    // Step 4: Verify and advance state.
    newCounter <- eventPrepared match
      case Left(error) => Future.successful(Left(error))
      case Right(event) => counter.effect(event)

    fakeReplicaId <- PrivateReplicaId()

    // Create and test fake events to verify that the effect verification is working.
    fakeEvent1 = EventWithContext(
      AddCounterEvent(5),
      CounterContext(fakeReplicaId, event.context.proofs)
    )

    fakeEvent2 = EventWithContext(
      AddCounterEvent(5),
      CounterContext(replicaId, Set.empty)
    )

    fakeEvent3 = EventWithContext(
      AddCounterEvent(4),
      CounterContext(replicaId, Set(ClaimProof(BinaryData("fakeProof".getBytes), CounterRoles.Adder)))
    )

    fakeValid1 <- testingPrepareAndEffect(counter, fakeEvent1).map(_.swap.toOption)
    fakeValid2 <- testingPrepareAndEffect(counter, fakeEvent2).map(_.swap.toOption)
    fakeValid3 <- testingPrepareAndEffect(counter, fakeEvent3).map(_.swap.toOption)
    fakeValid4 <- eventPrepared match
      case Left(error) => Future.successful(Left(error))
      case Right(event) => newCounter.toOption.get.effect(event).map(_.swap.toOption)

  do
    println(s"State is valid: ${newCounter.swap.toOption}")
    println(s"Fake state 1 is valid: $fakeValid1")
    println(s"Fake state 2 is valid: $fakeValid2")
    println(s"Fake state 3 is valid: $fakeValid3")
    println(s"Fake state 4 is valid: $fakeValid4")
    println(s"Old state was: ${counter.state.value}")
    println(s"New state is: ${newCounter.map(_.state.value)}")
    println(s"ecmrdt1")
*/