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
      linkComponent(displayValue, page),
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

def linkComponent(label: String, page: Page)(using services: Services) =
  val labelMaxLength = 25

  // We route manually (not via a href) to prevent unnecessary page reloads. We still
  // provide a href for opening in a new tab option.

  def routeToPage =
    services.routing.to(page)

  div(
    cls := "input flex items-center bg-base-200 text-lg w-full",
    readOnly := true,
    a(
      cls := "transition hover:text-secondary hover:cursor-pointer md:hidden",
      href := services.routing.link(page),
      label.size > labelMaxLength match
        case true  => label.take(labelMaxLength) + "..."
        case false => label,
      onClick.preventDefault.foreach(_ => routeToPage)
    ),
    a(
      cls := "transition hover:text-secondary hover:cursor-pointer hidden md:block",
      href := services.routing.link(page),
      label.size > labelMaxLength match
        case true  => label.take(labelMaxLength * 2) + "..."
        case false => label,
      onClick.preventDefault.foreach(_ => routeToPage)
    )
  )
