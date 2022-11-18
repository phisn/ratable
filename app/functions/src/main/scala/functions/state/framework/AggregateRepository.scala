package functions.state.framework

import scala.concurrent.*

trait AggregateRepository[A]:
  def get(id: String): Future[Option[A]]
  def applyDelta(id: String, aggregate: A): Future[Unit]
