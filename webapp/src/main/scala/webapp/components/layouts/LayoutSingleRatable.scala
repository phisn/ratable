package webapp.components.layouts

import core.state.aggregates.ratable.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.components.*
import webapp.components.layouts.*
import webapp.pages.homepage.*
import webapp.pages.viewpage.*
import webapp.services.*
import webapp.state.framework.{given, *}
import webapp.{given, *}

import kofre.base.*
import outwatch.BasicVNode

def layoutSingleRatable(ratableID: String)(body: Ratable => VNode)(using services: Services) =
  // val ratableSignal = services.state.ratables.listen(ratableID)
  
  // invalid id handling currently not implemented
  // ratableSignal.filter(_.isEmpty).foreach(_ => services.routing.to(HomePage()))
  
  // ratableSignal.foreach(_.changed.filter(_.isEmpty).observe(_ => services.routing.to(HomePage())))

  layoutComponent(
    div(
      cls := "flex-grow flex flex-col",
      services.state.ratables.map(ratableID)(
        div(
          "Loading"
        ),
        div(
          "Ratable not found"
        ),
        body
      )
    )
  )