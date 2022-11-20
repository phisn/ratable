package webapp.application.pages.sharepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.application.{given, *}
import webapp.application.components.*
import webapp.application.components.icons.*
import webapp.application.components.layouts.*
import webapp.application.pages.ratepage.*
import webapp.application.pages.viewpage.*
import webapp.services.*
import webapp.state.framework.*

def copyBoxComponent(title: String, page: Page)(using services: Services) = 
  val link = services.routing.link(page)
  val displayValue = link.stripPrefix("https://").stripPrefix("http://")
  val clickEvt = Evt[Unit]()

  clickEvt.observe { _ =>
    services.routing.to(page)
  }

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
      linkComponent(displayValue, clickEvt),
      button(
        cls := "btn btn-square btn-primary",
        onClick.foreach(_ => 
          dom.window.navigator.clipboard.writeText(link)
        ),
        iconCopy(
          cls := "w-12 h-6"
        )
      )
    )
  )

def linkComponent(label: String, clickEvt: Evt[Unit]) =
  val labelMaxLength = 25

  div(
    cls := "input flex items-center bg-base-200 text-lg w-full",
    readOnly := true,
    a(
      cls := "transition hover:text-secondary hover:cursor-pointer md:hidden",
      label.size > labelMaxLength match
        case true  => label.take(labelMaxLength) + "..."
        case false => label,
      onClick.as(()) --> clickEvt
    ),
    a(
      cls := "transition hover:text-secondary hover:cursor-pointer hidden md:block",
      label.size > labelMaxLength match
        case true  => label.take(labelMaxLength * 2) + "..."
        case false => label,
      onClick.as(()) --> clickEvt
    )
  )