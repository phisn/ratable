package webapp.services

import core.state.*
import core.state.aggregates.ratable.{*, given}
import webapp.services.*
import webapp.state.services.*
import webapp.state.{*, given}

import scala.reflect.Selectable.*

// Provides access to application state and all its facades
class StateProvider(services: {
  val applicationStateFactory: ApplicationStateFactory
  val statePersistence: StatePersistenceService
}):
  private val application = services.applicationStateFactory.buildApplicationState

  // Explicitly boot up the StatePersistenceService
  // after application state has been created
  services.statePersistence.boot

  def ratable(id: String) = application.ratables.facade(id).changes
  def ratable(id: String)(action: Ratable => Ratable) = application.ratables.facade(id).actions.fire(action)

  def ratables = application.ratables
