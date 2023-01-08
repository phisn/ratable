package webapp.services

import core.domain.aggregates.ratable.{*, given}
import core.framework.*
import java.util.UUID
import scala.reflect.Selectable.*
import webapp.services.*
import webapp.state.{*, given}
import webapp.state.framework.*
import webapp.state.services.*

// Provides access to application state and all its facades
class StateProvider(services: {
  val config: ApplicationConfigInterface
  val applicationStateFactory: ApplicationStateFactory
})(using Crypt):
  private val application = services.applicationStateFactory.buildApplicationState

  def ratables = application.ratables
  def library = application.library
