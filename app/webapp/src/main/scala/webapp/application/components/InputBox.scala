package webapp.application.pages.createpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.components.*
import webapp.application.components.layouts.*
import webapp.application.pages.homepage.*
import webapp.application.pages.viewpage.*
import webapp.services.*
import webapp.state.framework.{given, *}
import webapp.application.{given, *}
import webapp.application.usecases.ratable.*

def inputComponent(placeholderText: String, labelText: String, inputVar: Var[String]) =
  div(
    cls := "form-control",
    label(
      cls := "label",
      span(
        cls := "label-text",
        labelText
      ),
    ),
    input(
      cls := "input bg-base-200 w-full",
      placeholder := placeholderText,
      value <-- inputVar,
      onInput.value --> inputVar
    )
  )
