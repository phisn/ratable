package webapp.state.framework

import core.framework.ecmrdt.*
import core.messages.common.*
import rescala.default.{Signal, Signals}
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.*

trait AggregateViewRepository[A, C, E <: Event[A, C]]:
  def all: Future[Seq[(AggregateGid, A)]]
  
  def create(id: String, aggregate: A): Future[AggregateView[A, C, E]]
  def get(id: String): Future[Option[AggregateView[A, C, E]]]

  def map[B](id: String)(loading: B, notFound: B, found: A => B): Signal[B] =
    Signals.fromFuture(get(id))
      .map {
        case Some(ratable) => ratable.listen.map(found)
        case None => Signal(notFound)
      }
      .withDefault(Signal(loading))
      .flatten

  def effect(id: String, event: EventWithContext[A, C, E])(using EffectPipeline[A, C]): Future[Option[String]] =
    get(id)
      .flatMap {
        case Some(facade) => facade.effect(event)
        case None => Future.successful(None)
      }
