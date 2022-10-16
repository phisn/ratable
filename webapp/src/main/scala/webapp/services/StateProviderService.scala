package webapp.services

import kofre.decompose.containers.DeltaBufferRDT
import webapp.services.*
import webapp.store.*
import webapp.store.given
import webapp.store.aggregates.ratable.*
import webapp.store.framework.given

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
