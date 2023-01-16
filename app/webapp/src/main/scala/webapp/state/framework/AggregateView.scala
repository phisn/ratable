package webapp.state.framework

import cats.data.*
import core.framework.*
import core.framework.ecmrdt.*
import rescala.default.Signal
import scala.concurrent.*

// AggregateView exposes the manipulation or reading of the aggregate A
trait AggregateView[A, C, E <: Event[A, C]]:
  def effect(event: E, context: C)(using EffectPipeline[A, C]): EitherT[Future, RatableError, Unit]
  def listen: Signal[A]
