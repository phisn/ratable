package webapp.pages.homepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.pages.sharepage.*
import webapp.services.*
import webapp.store.aggregates.rating.{given, *}
import webapp.store.framework.*
import webapp.{*, given}

def ratableInputComponent(using services: Services) = 
  val onInput = Evt[String]()

  onInput.observe { value => 
    services.routing.to(SharePage(value))
  }

  div(
    desktopRatableInputComponent(onInput),
    mobileRatableInputComponent(onInput)
  )

private def desktopRatableInputComponent(onInput: Evt[String]) =
  div(
    cls := "hidden md:block form-control",
    width := "40rem",
    div(
      cls := "input-group",
      input(
        cls := "input bg-base-200 w-full",
        placeholder := "Rating of this great chinese food place"
      ),
      button(
        cls := "btn btn-primary",
        "Create",
        onClick.foreach(_ => onInput.fire("123"))
      )
    )
  )

private def mobileRatableInputComponent(onInput: Evt[String]) =
  div(
    cls := "visible md:hidden flex flex-col items-end space-y-4",
    input(
      cls := "input bg-base-200 w-full",
      placeholder := "Rating of this great chinese food place"
    ),
    button(
      cls := "btn btn-primary w-min",
      "Create",
        onClick.foreach(_ => onInput.fire("123"))
    )
  )
