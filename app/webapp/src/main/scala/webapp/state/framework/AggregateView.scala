package webapp.state.framework

import kofre.decompose.containers.DeltaBufferRDT
import rescala.default.*
import scala.concurrent.*

// AggregateView exposes the manipulation or reading of the aggregate A
trait AggregateView[A]:
  def mutate(f: A => A): Unit
  def listen: Signal[A]

object AggregateView:
  def fromFacade[A](aggregateFacade: AggregateFacade[A]): AggregateView[A] =
    new AggregateView:
      def mutate(f: A => A): Unit =
        aggregateFacade.mutationEvent.fire(f)

      def listen: Signal[A] =
        aggregateFacade.signal
