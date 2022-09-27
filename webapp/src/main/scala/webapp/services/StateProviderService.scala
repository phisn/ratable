package webapp.services

import kofre.decompose.containers.DeltaBufferRDT
import webapp.services.*
import webapp.store.ApplicationState
import webapp.store.aggregates.ratings.*

import scala.reflect.Selectable.*

// Provides access to application state and all its facades
class StateProviderService(services: {
  val stateDistribution: StateDistributionService
}):
  val state = ApplicationState(
    services.stateDistribution.registerAggregate[Ratings]
  )

  def ratings = state.ratings.changes
  def ratings(action: DeltaBufferRDT[Ratings] => DeltaBufferRDT[Ratings]) = state.ratings.actions.fire(action)
