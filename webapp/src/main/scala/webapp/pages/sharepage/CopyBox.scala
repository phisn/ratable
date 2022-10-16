package webapp.pages.sharepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.icons.*
import webapp.services.*
import webapp.store.aggregates.rating.{given, *}
import webapp.store.framework.*
import webapp.{*, given}

def copyBoxComponent(title: String, content: String)(using services: Services) = 
  val displayValue = content.stripPrefix("https://").stripPrefix("http://")
  val displayValueMaxLength = 25

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
          displayValue.size > displayValueMaxLength match
            case true  => displayValue.take(displayValueMaxLength) + "..."
            case false => displayValue
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
