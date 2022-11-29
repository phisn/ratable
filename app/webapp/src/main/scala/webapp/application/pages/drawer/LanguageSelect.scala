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

