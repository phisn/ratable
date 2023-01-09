package webapp.state.framework

import cats.data.*
import core.framework.*
import core.framework.ecmrdt.*
import core.messages.common.*
import rescala.default.{Signal, Signals}
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.*

trait AggregateViewRepository[A, C, E <: Event[A, C]]:
  def all: Future[Seq[(AggregateGid, A)]]
  
  def create(id: AggregateId, aggregate: A): Future[AggregateView[A, C, E]]
  def get(id: AggregateId): OptionT[Future, AggregateView[A, C, E]]

  def getEnsure(id: AggregateId): EitherT[Future, RatableError, AggregateView[A, C, E]] =
    get(id).toRight(RatableError("Aggregate not found"))

  def getOrCreate(id: AggregateId, aggregate: => A): Future[AggregateView[A, C, E]] =
    get(id).getOrElseF(create(id, aggregate))

  def map[B](id: AggregateId)(loading: B, notFound: B, found: A => B): Signal[B] =
    Signals.fromFuture(get(id).value)
      .map {
        case Some(ratable) => ratable.listen.map(found)
        case None => Signal(notFound)
      }
      .withDefault(Signal(loading))
      .flatten

  def effect(id: AggregateId, event: EventWithContext[A, C, E])(using EffectPipeline[A, C]): OptionT[Future, RatableError] =
    for
      view <- get(id)
      result <- view.effect(event)
    yield
      result
