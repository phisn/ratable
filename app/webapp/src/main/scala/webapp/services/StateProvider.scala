package webapp.services

import core.domain.aggregates.ratable.{*, given}
import java.util.UUID
import scala.reflect.Selectable.*
import webapp.services.*
import webapp.state.services.*
import webapp.state.{*, given}

import webapp.state.framework.*

// Provides access to application state and all its facades
class StateProvider(services: {
  val config: ApplicationConfigInterface
  val applicationStateFactory: ApplicationStateFactory
}):
  private val application = services.applicationStateFactory.buildApplicationState

  def ratables = application.ratables

  def uniqueID: String = UUID.randomUUID().toString()
