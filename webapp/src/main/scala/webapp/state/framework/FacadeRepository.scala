package webapp.state.framework

import rescala.default.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.*

trait FacadeRepository[A]:
  def create(id: String, aggregate: A): Unit
  def get(id: String): Future[Option[Facade[A]]]

  def map[B](id: String)(loading: B, notFound: B, found: A => B): Signal[B] =
    Signals.fromFuture(get(id))
      .map(_ match
        case Some(ratable) => ratable.changes.map(found)
        case None => Signal(notFound)
      )
      .withDefault(Signal(loading))
      .flatten
 
  def mutate(id: String, action: A => A): Future[Unit] =
    get(id)
      .map(
        _.getOrElse(throw new Exception(s"Aggregate with id $id not found"))
      )
      .andThen {
        case Success(facade) => facade.actions.fire(action)
        case _ =>
      }
      .map(_ => ())
