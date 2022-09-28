package webapp.store

import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import rescala.default.*

// Boundles access to all aggregates as facades in one central application state
// Used to manipulate or read application state
case class ApplicationState(
  ratings: Facade[Ratings]
):
  def toDTO = ApplicationStateDTO(ratings.changes.now)
  def toSignalDTO = Signal.dynamic(ApplicationStateDTO(
    ratings.changes.value
  ))

case class ApplicationStateDTO(
  ratings: Ratings
)
