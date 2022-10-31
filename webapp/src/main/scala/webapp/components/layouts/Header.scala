package webapp.components.layouts

import org.scalajs.dom.*

import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.icons.*
import webapp.pages.homepage.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def headerComponent(using services: Services) =
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
      button(
        cls := "btn btn-ghost btn-square",
        iconSearch(
          cls := "w-8 h-8",
        )
      )
    )
  )

private def logoComponent(using services: Services) =
  a(
    cls := "btn btn-ghost normal-case text-2xl",
    "Ratable",
    onClick.foreach(_ => services.routing.to(HomePage()))
  )
