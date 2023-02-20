package core.framework.ecmrdt.example.owner

import cats.data.*
import cats.implicits.*
import core.framework.*
import core.framework.ecmrdt.*
import core.framework.ecmrdt.extensions.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

enum CounterClaimEnum:
  case None

case class Counter(
  val value: Int,
) 

case class CounterContext(
  val replicaId: ReplicaId,
)  extends IdentityContext

object Counter:
  given EffectPipeline[Counter, CounterContext] = EffectPipeline(
    SingleOwnerEffectPipeline()
  )

sealed trait CounterEvent extends Event[Counter, CounterContext]

case class AddCounterEvent() extends CounterEvent:
  def asEffect =
    (state, context, meta) => EitherT.pure(
      state.copy(value = state.value + 1)
    )

def main(using Crypt) = 
  for
    // Step 1: Create replicaId
    replicaId <- EitherT.liftF(PrivateReplicaId())

    // Step 2: Create initial state.
    counter = ECmRDT[Counter, CounterContext, CounterEvent](Counter(0))

    // Step 3: Create event.
    eventPrepared = counter.prepare(
      AddCounterEvent(),
      CounterContext(replicaId)
    )

    // Step 4: Verify and advance state.
    newCounter <- counter.effect(eventPrepared, MetaContext(null, null))

  yield
    println(s"Old counter: ${counter.state.value}")    // 0
    println(s"New counter: ${newCounter.state.value}") // 1
