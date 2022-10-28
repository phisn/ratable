package webapp.services

import core.store.*
import core.store.aggregates.ratable.{*, given}
import webapp.services.*
import webapp.store.{*, given}

import scala.reflect.Selectable.*

// Provides access to application state and all its facades
class StateProvider(services: {
  val facadeFactory: FacadeFactory
}):
  val application = ApplicationState(
    services.facadeFactory.registerAggregate[RatableRepository](AggregateId.Ratable.toString()),
  )

  def ratables = application.ratables.changes
  def ratables(action: RatableRepository => RatableRepository) = application.ratables.actions.fire(action)
