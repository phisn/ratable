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
        centerContentComponent(
          div(
            cls := "flex flex-col space-y-8 items-center",
            div(
              cls := "text-2xl font-bold",
              "Loading",
            ),
            VNode.html("progress")(
              cls := "progress w-[20rem]"
            )
          )
        ),
        centerContentComponent(
          div(
            cls := "flex flex-col space-y-8 items-center",
            titleComponent("Ratable not found"),
            button(
              cls := "btn btn-ghost",
              "Create a new one",
              onClick.foreach(_ => services.routing.to(HomePage()))
            )
          )
        ),
        body
      )
    )
  )