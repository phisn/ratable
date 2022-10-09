package webapp.pages.homepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.pages.sharepage.*
import webapp.services.*
import webapp.store.aggregates.ratings.given
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.{*, given}

def ratableInput(using services: Services) = 
  div(
    cls := "form-control",
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
        onClick.foreach(_ => services.routing.to(SharePage("123")))
      )
    )
  )