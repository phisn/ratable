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
}):
  val application = services.applicationStateFactory.buildApplicationState

  def ratable(id: String) = application.ratables.facade(id).changes
  def ratable(id: String)(action: Ratable => Ratable) = application.ratables.facade(id).actions.fire(action)

/*
state.ratable(ratableId).changes
state.ratable(ratableId).mutate(_ => Ratable(...))
*/
