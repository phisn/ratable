package webapp.application.components.layouts

import core.domain.aggregates.ratable.*
import core.framework.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.*
import webapp.application.*
import webapp.application.framework.{given, *}
import webapp.application.components.*
import webapp.application.components.common.*
import webapp.application.components.layouts.*
import webapp.application.pages.homepage.*
import webapp.application.pages.viewpage.*
import webapp.services.*
import webapp.state.framework.{given, *}

import kofre.base.*
import outwatch.BasicVNode
import core.domain.aggregates.ratable.Ratable
import core.domain.aggregates.ratable.Ratable

def layoutSingleRatable(ratableId: AggregateId)(body: Ratable => VNode)(using services: ServicesWithApplication) =
  layoutComponent(
    div(
      cls := "flex-grow flex flex-col",
      services.state.ratables.map(ratableId)(
        contentFullCenterComponent(
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
        contentFullCenterComponent(
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