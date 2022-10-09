package webapp.components.layouts

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.pages.homepage.*
import webapp.services.*
import webapp.store.aggregates.ratings.given
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.{*, given}

def headerComponent(using services: Services) =
  div(
    cls := "navbar bg-base-200 p-4",
    div(
      cls := "flex-1",
      a(
        cls := "btn btn-ghost normal-case text-2xl",
        "Ratable",
        onClick.foreach(_ => services.routing.to(HomePage()))
      )
    ),
    div(
      cls := "flex-none",
      button(
      cls := "btn btn-square btn-ghost",
      img(
        cls := "w-8 h-8",
        src := "/icons/search.svg"
      )
    )
    )
  )