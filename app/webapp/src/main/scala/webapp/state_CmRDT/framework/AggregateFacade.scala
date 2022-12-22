package webapp.state.framework

import core.framework.*
import core.framework.ecmrdt.*
import rescala.default.*
import scala.concurrent.*

// Internal representation of an aggregate
case class AggregateFacade[A, C](
  val signal: Signal[DeltaContainer[A]],

  val effectEvent: Evt[EventWithContext[A, C]],
  val deltaEvent: Evt[A],
  val deltaAckEvent: Evt[Tag],
):
  def toView: AggregateView[A, C] =
    new AggregateView:
      def effect(event: EventWithContext[A, C]): Unit =
        effectEvent.fire(event)

      def listen: Signal[A] =
        signal.map(_.inner)
