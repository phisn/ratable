package functions.state.framework

trait AggregateRepository[A]:
  def get(id: String): Option[A]
  def set(id: String, aggregate: A): Unit
