package webapp.application.components.layouts

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.components.icons.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def headerCleanComponent(using services: Services) =
  div(
    cls := "navbar",
    div(
      cls := "flex-1",
    ),
    div(
      cls := "flex-none",
      label(
        cls := "drawer-button btn btn-square btn-ghost",
        forId := "main-drawer",
        iconSearch(
          cls := "w-8 h-8",
        )
      )
    )
  )
