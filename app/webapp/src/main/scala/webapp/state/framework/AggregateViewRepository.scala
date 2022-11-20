package webapp.state.framework

import rescala.default.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.*

trait AggregateViewRepository[A]:
  def create(id: String, aggregate: A): Unit
  def get(id: String): Future[Option[Facade[A]]]

  def map[B](id: String)(loading: B, notFound: B, found: A => B): Signal[B] =
    Signals.fromFuture(get(id))
      .map {
        case Some(ratable) => ratable.listen.map(found)
        case None => Signal(notFound)
      }
      .withDefault(Signal(loading))
      .flatten

  def mutate(id: String, action: A => A): Future[Unit] =
    get(id)
      .andThen {
        case Success(Some(facade)) => facade.mutate(action)
      }
      .map {
        case Some(facade) => ()
        case None => throw Exception(s"Aggregate with id $id not found")
      }
