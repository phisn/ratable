package webapp.store

import core.store.aggregates.ratable.*
import webapp.store.framework.*
import rescala.default.*

// Boundles access to all aggregates as facades in one central application state
// Used to manipulate or read application state
case class ApplicationState(
  ratables: Facade[RatableRepository]
):
  def toDTO = ApplicationStateDTO(
    ratables.changes.now
  )

  def toSignalDTO = Signal.dynamic(ApplicationStateDTO(
    ratables.changes.value
  ))

case class ApplicationStateDTO(
  ratables: RatableRepository
)
