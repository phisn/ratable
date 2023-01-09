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
  
  def create(id: AggregateId, aggregate: A): EitherT[Future, RatableError, AggregateView[A, C, E]]
  def get(id: AggregateId): EitherT[Future, RatableError, Option[AggregateView[A, C, E]]]

  def getEnsure(id: AggregateId): EitherT[Future, RatableError, AggregateView[A, C, E]] =
    get(id).flatMap {
      case Some(view) => EitherT.rightT(view)
      case None => EitherT.leftT(RatableError(s"Aggregate with id: '$id' not found"))
    }

  def getOrCreate(id: AggregateId, aggregate: => A) =
    getEnsure(id).orElse(create(id, aggregate))

  def map[B](id: AggregateId)(loading: B, notFound: B, found: A => B): Signal[B] =
    Signals.fromFuture(get(id).value)
      .map {
        case Right(None) => 
          println(s"Aggregate with id: '$id' not found")
          Signal(notFound)
        case Right(Some(view)) => 
          println(s"Aggregate with id: '$id' found")
          view.listen.map(found)

        // TODO: Should have its seperate page
        case Left(error) => 
          println(s"Error: $error")
          throw Exception(error.default)
      }
      .withDefault(Signal(loading))
      .flatten

  def effect(id: AggregateId, event: EventWithContext[A, C, E])(using EffectPipeline[A, C]): EitherT[Future, RatableError, Unit] =
    for
      view <- getEnsure(id)
      result <- view.effect(event)
    yield
      result
