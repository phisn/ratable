package webapp.services

import kofre.decompose.containers.DeltaBufferRDT
import webapp.services.*
import webapp.store.LocalRatingState
import webapp.store.aggregates.*

import scala.reflect.Selectable.*

class StateProviderService(services: {
  val stateDistribution: StateDistributionService
}):
  val state = LocalRatingState(
    services.stateDistribution.registerRepository[Ratings]
  )

  def ratings = state.ratings.changes
  def ratings(action: DeltaBufferRDT[Ratings] => DeltaBufferRDT[Ratings]) = state.ratings.actions.fire(action)

