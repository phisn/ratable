package functions.state.services

import core.messages.common.*
import functions.state.*
import functions.state.framework.*
import scala.scalajs.js

trait AggregateRepositoryFactoryInterface:
  def create[A](aggregateType: AggregateType): AggregateRepository[A]

class AggregateRepositoryFactory(
  services: {},
  context: js.Dynamic
) extends AggregateRepositoryFactoryInterface:
  def create[A](aggregateType: AggregateType): AggregateRepository[A] =
    new AggregateRepository[A]:
      override def get(id: String): Option[A] =
        None

      override def set(id: String, aggregate: A): Unit =
        ()
