package webapp.application.pages.drawer

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

def drawer(using services: ServicesWithApplication) =
  div(
    cls := "drawer-side overflow-hidden",
    label(
      forId := "main-drawer",
      cls := "drawer-overlay"
    ),
    div(
      cls := "flex flex-col p-4 pr-0 w-full md:w-[24rem] bg-base-100 text-base-content max-h-screen",
      div(
        cls := "flex justify-between pr-4",
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
        cls := "flex-grow overflow-y-auto my-4 pr-4",
        cls := "md:scrollbar-thin md:scrollbar-track-base-200 md:scrollbar-thumb-base-content",
        ratableInfiniteScrollerComponent
      ),
      div(
        cls := "flex justify-evenly pr-4",
        languageSelectButton("English", "en"),
        languageSelectButton("Deutsch", "de")
      )
    )
  )
