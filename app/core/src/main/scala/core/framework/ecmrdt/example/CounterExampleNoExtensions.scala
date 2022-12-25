package core.framework.ecmrdt.example.noextensions

import core.framework.*
import core.framework.ecmrdt.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

case class Counter(
  val value: Int,
) 
/*
case class CounterContext(
  val replicaId: String,
)  extends IdentityContext 

object Counter:
  given (using Crypt): EffectPipeline[Counter, CounterContext] = EffectPipeline()

case class AddCounterEvent(
  val value: Int
) extends Event[Counter, CounterContext]:
  def asEffect: Effect[Counter, CounterContext] =
    Effect.from(
      (state, context) => Option.when(value < 0)(s"Value must be positive."),
      (state, context) => state.copy(value = state.value + value)
    )

def addCounterEvent(replicaId: String, value: Int) =
  EventWithContext(
    AddCounterEvent(value),
    CounterContext(replicaId)
  )
  
def main = 
  val replicaId = "replicaId"

  val counter = ECmRDT[Counter, CounterContext](Counter(0))
  val event = addCounterEvent(replicaId, 5)

  for
    // Step 2: Create initial state.

    // Step 3: Create event.
    eventPrepared <- counter.prepare(event)

    // Step 4: Verify and advance state.
    newCounter <- eventPrepared match
      case Left(error) => Future.successful(Left(error))
      case Right(event) => counter.effect(event)

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