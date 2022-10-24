package webapp.components.layouts

import core.store.aggregates.ratable.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.*
import webapp.components.layouts.*
import webapp.pages.homepage.*
import webapp.pages.viewpage.*
import webapp.services.*
import webapp.store.framework.{given, *}
import webapp.{given, *}

def layoutSingleRatable(ratableID: String)(body: Ratable => VNode)(using services: Services) =
  val ratableSignal = services.stateProvider.ratables.map(_.get(ratableID))
  
  // invalid id handling currently not implemented
  ratableSignal.changed.filter(_.isEmpty).observe(_ => services.routing.to(HomePage()))

  layoutComponent(
    div(
      cls := "flex-grow flex flex-col",
      ratableSignal.map( ratable =>
        if ratable.isEmpty then
          div(
            "Loading"
          )
        else
          body(ratable.get)
      )
    )
  )

