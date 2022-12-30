package webapp.state.framework

import core.framework.ecmrdt.*
import rescala.default.Signal
import scala.concurrent.*

// AggregateView exposes the manipulation or reading of the aggregate A
trait AggregateView[A, C, E <: Event[A, C]]:
  def effect(event: EventWithContext[A, C, E])(using EffectPipeline[A, C]): Future[Option[String]]
  def listen: Signal[A]
