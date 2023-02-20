package core.framework.ecmrdt.example

import cats.data.*
import cats.implicits.*
import core.framework.*
import core.framework.ecmrdt.*
import core.framework.ecmrdt.extensions.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

enum CounterClaimEnum:
  case Adder

case class Counter(
  val value: Int,
  val claims: List[Claim[CounterClaimEnum]]
) 
extends AsymPermissionStateExtension[CounterClaimEnum]

case class CounterContext(
  val replicaId: ReplicaId,
  val proofs: List[ClaimProof[CounterClaimEnum]]
) 
extends IdentityContext 
   with AsymPermissionContextExtension[CounterClaimEnum]

object Counter:
  given (using Crypt): EffectPipeline[Counter, CounterContext] = EffectPipeline(
    AsymPermissionEffectPipeline[Counter, CounterClaimEnum, CounterContext]
  )

sealed trait CounterEvent extends Event[Counter, CounterContext]

case class AddCounterEvent(
  val value: Int
) extends CounterEvent:
  def asEffect: Effect[Counter, CounterContext] =
    (state, context, meta) => 
      for
        _ <- context.verifyClaim(CounterClaimEnum.Adder)
      yield
        state.copy(value = state.value + value)

def main(using Crypt) = 
  for
    replicaId <- EitherT.liftF(PrivateReplicaId())

    // Step 1: Create claims and claimProvers.
    claimsPair <- EitherT.liftF(
      Claim.create(List(CounterClaimEnum.Adder))
    )

    (claims, provers) = claimsPair

    proof <- EitherT.liftF(
      provers.head.prove(replicaId)
    )

    // Step 2: Create initial state.
    counter = ECmRDT[Counter, CounterContext, CounterEvent](Counter(0, claims))

    // Step 3: Create event.
    eventPrepared = counter.prepare(
      AddCounterEvent(5),
      CounterContext(replicaId, List(proof))
    )

    // Step 4: Verify and advance state.
    newCounter <- counter.effect(eventPrepared, MetaContext(null, null))

  yield
    println(s"New counter: $newCounter")
