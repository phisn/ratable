package webapp.application.components.layouts

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.*
import webapp.application.components.*
import webapp.application.components.icons.*
import webapp.application.components.popups.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def headerCleanComponent(using services: ServicesWithApplication) =
  div(
    cls := "navbar",
    div(
      cls := "flex-1",
      div(
        cls := "drawer-button btn btn-square btn-ghost",
        iconInfo(
          cls := "w-8 h-8",
          onClick.foreach(_ => services.popup.show(InfoPopup()))
        ),
      )
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
