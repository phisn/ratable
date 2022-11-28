package webapp.application.components.layouts

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.*
import webapp.application.framework.{given, *}
import webapp.application.components.*
import webapp.application.components.icons.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def layoutCustomHeaderComponent(header: VNode)(body: VNode)(using services: ServicesWithApplication) =
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
        cls := "flex flex-col p-4 w-full md:w-80 bg-base-100 text-base-content",
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
        ),
        div(
          cls := "flex-grow",
          // "content"
        ),
        div(
          cls := "flex justify-evenly",
          languageSelectButton("English", "en"),
          languageSelectButton("Deutsch", "de")
        )
      )
    ),
    services.popup.render.map(_.map(popup =>
      div(
        popup
      )
    ))
  )

def languageSelectButton(label: String, language: String)(using services: ServicesWithApplication) =
  button(
    cls := "btn",

    services.config.language.map(lang =>
      if lang.contains(language) then
        cls := "btn-outline"
      else
        cls := "btn-ghost"
    ),

    label,

    onClick.foreach(_ =>
      services.config.language.set(language)
    )
  )
