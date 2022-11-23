package webapp.application.pages.homepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.application.{given, *}
import webapp.application.framework.*
import webapp.application.pages.createpage.*
import webapp.services.*
import webapp.state.framework.*
import webapp.application.usecases.ratable.*

def ratableInputComponent(using services: Services) = 
  implicit val form = FormValidation()

  val title = form.validateVar("", _.length > 0)

  div(
    cls := "form-control md:w-[40rem]",
    label(
      cls := "label",
      span(
        cls := "label-text",
        "Title of your Ratable"
      ),
    ),
    div(
      cls := "flex flex-col md:flex-row md:input-group space-y-4 md:space-y-0",
      input(
        title.state.map {
          case ValidationState.None  => cls := ""
          case ValidationState.Error => cls := "border-red-500 border-2"
        },
        cls := "input bg-base-200 w-full",
        placeholder := "Rating of this great chinese food place",

        onInput.value --> title.variable
      ),
      button(
        cls := "btn btn-primary",
        "Create",
        onClick.filter(_ => form.validate).foreach(_ =>
          services.routing.to(CreatePage(title.variable.now), true)
        )
      )
    )
  )
