package webapp.pages.sharepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.icons.*
import webapp.services.*
import webapp.store.aggregates.ratings.given
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.{*, given}

def copyBoxComponent(title: String, content: String)(using services: Services) = 
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
      div(
        cls := "input flex items-center bg-base-200 text-lg w-full",
        readOnly := true,
        a(
          cls := "transition hover:text-secondary",
          href := content,
          target := "_blank",
          content
        )
      ),
      button(
        cls := "btn btn-square btn-primary",
        onClick.foreach(_ => 
          dom.window.navigator.clipboard.writeText(content)
        ),
        iconCopy(
          cls := "w-12 h-6"
        )
      )
    )
  )
