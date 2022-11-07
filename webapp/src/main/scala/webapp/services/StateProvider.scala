package webapp.services

import core.state.*
import core.state.aggregates.ratable.{*, given}
import webapp.services.*
import webapp.state.services.FacadeFactory
import webapp.state.{*, given}

import scala.reflect.Selectable.*

// Provides access to application state and all its facades
class StateProvider(services: {
  val facadeFactory: FacadeFactory
}):
  services.statePersistence
    .migrationsFor[Ratable](AggregateId.Ratable.toString())
    .build

  val application = ApplicationState(
    services.facadeFactory.registerAggregateAsRepository[Ratable](AggregateId.Ratable.toString()),
  )

  def ratables = application.ratables.changes
  def ratables(action: RatableRepository => RatableRepository) = application.ratables.actions.fire(action)
