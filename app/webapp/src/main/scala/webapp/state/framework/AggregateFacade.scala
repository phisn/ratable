package webapp.state.framework

import core.framework.*
import rescala.default.*
import scala.concurrent.*

// Internal representation of an aggregate
case class AggregateFacade[A](
  val signal: Signal[A],

  val mutationEvent: Evt[A => A],
  val deltaEvent: Evt[A],
  val deltaAckEvent: Evt[Tag],
):
  def toView: AggregateView[A] =
    new AggregateView:
      def mutate(f: A => A): Unit =
        mutationEvent.fire(f)

      def listen: Signal[A] =
        signal
