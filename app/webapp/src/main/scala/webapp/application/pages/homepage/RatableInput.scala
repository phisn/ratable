package webapp.application.pages.homepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.{given, *}
import webapp.application.pages.sharepage.*
import webapp.services.*
import webapp.state.framework.*
import webapp.application.usecases.ratable.*

def ratableInputComponent(using services: Services) = 
  val customInputClass = Var("")

  val inputSignal = Var("")
  val inputEvt = Evt[String]()

  inputEvt.observe { value =>
    if value.isEmpty() then
      customInputClass.set("border-red-500 border-2")
    else
      val id = createRatable(value, List("Taste", "Ambiente", "Price"))
      services.routing.to(SharePage(id), true)
  }

  div(
    cls := "form-control md:w-[40rem]",
    div(
      cls := "flex flex-col md:flex-row items-end md:input-group space-y-4 md:space-y-0",
      input(
        customInputClass.map(cls := _),
        cls := "input bg-base-200 w-full",
        placeholder := "Rating of this great chinese food place",
        onInput.value --> inputSignal
      ),
      button(
        cls := "btn btn-primary",
        "Create",
        // TODO: Make button enter pressable
        onClick.foreach(_ => inputEvt.fire(inputSignal.now))
      )
    )
  )
