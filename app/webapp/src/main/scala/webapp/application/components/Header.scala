package webapp.application.components

import org.scalajs.dom.*

import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.*
import webapp.application.components.icons.*
import webapp.application.pages.homepage.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def headerComponent(using services: ServicesWithApplication) =
  div(
    cls := "navbar bg-base-200",
    div(
      cls := "flex-1",
      if services.routing.state.canReturn then
        a(
          cls := "btn btn-ghost btn-square",
          iconArrowLeftShort(
            cls := "w-8 h-8",
          ),
          onClick.foreach(_ => services.routing.back)
        )
      else
        logoComponent
    ),
    div(
      cls := "flex-none",
      label(
        cls := "drawer-button btn btn-ghost btn-square",
        forId := "main-drawer",
        iconSearch(
          cls := "w-8 h-8",
        )
      )
    )
  )

private def logoComponent(using services: ServicesWithApplication) =
  a(
    cls := "btn btn-ghost normal-case text-2xl",
    "Ratable",
    onClick.foreach(_ => services.routing.to(HomePage()))
  )
