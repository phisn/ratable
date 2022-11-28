package webapp.application.components.layouts

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.{given, *}
import webapp.application.components.*
import webapp.application.components.icons.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def layoutCustomHeaderComponent(header: VNode)(body: VNode)(using services: Services) =
  div(
    cls := "drawer drawer-end",
    input(
      idAttr := "main-drawer",
      tpe := "checkbox",
      cls := "drawer-toggle",
    ),
    div(
      cls := "drawer-content flex flex-col min-h-screen bg-base-100",
      header,
      div(
        cls := "flex-grow flex flex-col",
        body
      ),
      footerComponent
    ),
    div(
      cls := "drawer-side overflow-hidden",
      label(
        forId := "main-drawer",
        cls := "drawer-overlay"
      ),
      div(
        cls := "p-4 w-full md:w-80 bg-base-100 text-base-content",
        div(
          cls := "flex justify-between",
          label(
            cls := "drawer-button btn btn-ghost btn-square",
            forId := "main-drawer",
            iconArrowLeftShort(
              cls := "w-8 h-8",
            ),
          ),
          label(
            cls := "swap swap-rotate p-2",
            input(
              tpe := "checkbox",
              checked := services.config.darkMode.now,
              onChange.foreach(_ =>
                services.config.darkMode.set(!services.config.darkMode.now)
              )
            ),
            iconMoon(
              cls := "swap-on w-8 h-8",
            ),
            iconSun(
              cls := "swap-off w-8 h-8",
            ),
          )
        )
        /*
        ul(
          cls := "menu",
          li(a("test1")),
          li(a("test2")),
          li(a("test3")),
        )
        */
      )
    ),
    services.popup.render.map(_.map(popup =>
      div(
        popup
      )
    ))
  )
