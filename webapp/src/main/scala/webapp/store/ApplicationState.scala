package webapp.store

import webapp.store.aggregates.rating.*
import webapp.store.aggregates.ratable.*
import webapp.store.framework.*
import rescala.default.*

// Boundles access to all aggregates as facades in one central application state
// Used to manipulate or read application state
case class ApplicationState(
  ratings: Facade[RatingRepository],
  ratables: Facade[RatableRepository]
):
  def toDTO = ApplicationStateDTO(
    ratings.changes.now,
    ratables.changes.now
  )

  def toSignalDTO = Signal.dynamic(ApplicationStateDTO(
    ratings.changes.value,
    ratables.changes.value
  ))

case class ApplicationStateDTO(
  ratings: RatingRepository,
  ratables: RatableRepository
)
