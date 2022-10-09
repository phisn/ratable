package webapp.components.layouts

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.icons.*
import webapp.services.*
import webapp.store.aggregates.ratings.given
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.{*, given}

def headerCleanComponent(using services: Services) =
  div(
    cls := "navbar p-4",
    div(
      cls := "flex-1",
    ),
    div(
      cls := "flex-none",
      button(
        cls := "btn btn-square btn-ghost",
        iconSearch(
          cls := "w-8 h-8",
        )
      )
    )
  )
