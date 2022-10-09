package webapp.services

import colibri.*
import colibri.router.*
import colibri.router.Router
import org.scalajs.dom.*
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.pages.*

trait Page:
  def render(using services: Services): HtmlVNode

class RoutingService:
  private val page = Var[Page](Routes.fromPath(Path(window.location.pathname)))

  def render(using services: Services): Signal[HtmlVNode] =
    page.map(_.render)

  def to(newPage: Page) =
    window.history.pushState(null, "", linkPath(newPage))
    page.set(newPage)

  def toReplace(newPage: Page) =
    window.history.replaceState(null, "", linkPath(newPage))
    page.set(newPage)

  def link(newPage: Page) =
    URL(linkPath(newPage), window.location.href).toString

  def linkPath(newPage: Page) =
    Routes.toPath(newPage).pathString

  // Ensure initial path is correctly set
  // Example: for path "/counter" and pattern "counter/{number=0}" the
  //          url should be "/counter/0" and not "/counter"
  window.history.replaceState(null, "", linkPath(page.now))

  // Change path when url changes by user action
  window.onpopstate = _ => page.set(Routes.fromPath(Path(window.location.pathname)))
