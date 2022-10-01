package webapp.services

import kofre.decompose.containers.DeltaBufferRDT
import webapp.services.*
import webapp.store.*
import webapp.store.given
import webapp.store.aggregates.ratings.*
import webapp.store.framework.given

import scala.reflect.Selectable.*

// Provides access to application state and all its facades
class StateProviderService(services: {
  val stateDistribution: StateDistributionService
}):
  val state = ApplicationState(
    services.stateDistribution.registerAggregate[Ratings]("ratings")
  )

  def ratings = state.ratings.changes
  def ratings(action: Ratings => Ratings) = state.ratings.actions.fire(action)
