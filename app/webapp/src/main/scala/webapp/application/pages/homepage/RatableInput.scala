package webapp.application.pages.homepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.application.*
import webapp.application.framework.{given, *}
import webapp.application.framework.*
import webapp.application.pages.createpage.*
import webapp.services.*
import webapp.state.framework.*
import webapp.application.usecases.ratable.*

def ratableInputComponent(using services: ServicesWithApplication) = 
  implicit val form = FormValidation()

  val title = form.validatePromise("", _.length > 0)

  div(
    cls := "form-control",
    label(
      cls := "label",
      span(
        cls := "label-text",
        services.local.get("page.home.input.label")
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
        
        services.local.get("page.home.input.placeholder").map(
          placeholder := _
        ),

        onInput.value --> title.signal
      ),
      button(
        cls := "btn btn-primary",
        services.local.get("page.home.input.button"),
        onClick.filter(_ => form.validate).foreach(_ =>
          services.routing.to(CreatePage(title.signal.now), true)
        )
      )
    )
  )
