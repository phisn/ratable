package webapp.state.framework

import cats.data.*
import core.framework.*
import core.framework.ecmrdt.*
import rescala.default.Signal
import scala.concurrent.*

// AggregateView exposes the manipulation or reading of the aggregate A
trait AggregateView[A, C, E <: Event[A, C]]:
  def effect(event: EventWithContext[A, C, E])(using EffectPipeline[A, C]): EitherT[Future, RatableError, Nothing]
  def listen: Signal[A]
