package webapp.services

import core.store.aggregates.ratable.{*, given}
import webapp.services.*
import webapp.store.{*, given}

import scala.reflect.Selectable.*

// Provides access to application state and all its facades
class StateProviderService(services: {
  val stateDistribution: StateDistributionService
}):
  val state = ApplicationState(
    services.stateDistribution.registerAggregate[RatableRepository]("ratables")
  )

  def ratables = state.ratables.changes
  def ratables(action: RatableRepository => RatableRepository) = state.ratables.actions.fire(action)
