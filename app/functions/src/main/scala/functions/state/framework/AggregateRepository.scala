package functions.state.framework

import scala.concurrent.*

trait AggregateRepository[A]:
  def get(id: String): Future[Option[A]]
  def set(id: String, aggregate: A): Future[Unit]

  // def applyDelta(id: String, aggregate: A): Future[Unit]
