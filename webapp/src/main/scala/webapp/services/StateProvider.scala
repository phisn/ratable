package webapp.services

import core.state.*
import core.state.aggregates.ratable.{*, given}
import java.util.UUID
import scala.reflect.Selectable.*
import webapp.services.*
import webapp.state.services.*
import webapp.state.{*, given}

// Provides access to application state and all its facades
class StateProvider(services: {
  val config: ApplicationConfigInterface
  val applicationStateFactory: ApplicationStateFactory
  val statePersistence: StatePersistenceServiceInterface
}):
  private val application = services.applicationStateFactory.buildApplicationState

  // Explicitly boot up the StatePersistenceService
  // after application state has been created
  services.statePersistence.boot

  def ratable(id: String) = application.ratables.facade(id).changes
  def ratable(id: String)(action: Ratable => Ratable) = application.ratables.facade(id).actions.fire(action)

  def uniqueID = services.config.replicaID.take(4) + "-" + UUID.randomUUID()
