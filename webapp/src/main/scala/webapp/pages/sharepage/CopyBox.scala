package webapp.pages.sharepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.store.aggregates.ratings.given
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.{*, given}

def copyBox(title: String, content: String)(using services: Services) = 
  div(
    cls := "form-control",
    label(
      cls := "label",
      span(
        cls := "label-text",
        title
      ),
    ),
    div(
      cls := "input-group",
      input(
        cls := "input read-only w-full",
        readOnly := true,
        value := content
      ),
      button(
        cls := "btn btn-secondary",
        "Create",
        onClick.foreach(_ => services.routing.to(SharePage("123")))
      )
    )
  )
