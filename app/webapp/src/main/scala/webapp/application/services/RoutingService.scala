package webapp.application.services

import colibri.*
import colibri.router.*
import colibri.router.Router
import org.scalajs.dom.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.reflect.Selectable.*
import scala.scalajs.js
import webapp.*
import webapp.services.*
import webapp.application.*
import webapp.application.framework.{given, *}
import webapp.application.pages.*
import webapp.device.services.*

trait Page:
  def render(using services: ServicesWithApplication): VNode
  
class RoutingState(
  // if canReturn is true then the page will show in mobile mode
  // an go back arrow in the top left corner
  val canReturn: Boolean = false,
  val silent: Boolean = false,
  val drawerOpened: Boolean = false

) extends js.Object

class RoutingService(services: {
  val logger: LoggerServiceInterface
  val window: WindowServiceInterface
}):
  private val page = Var[Page](Routes.fromPath(Path(services.window.routePath)))

  private val popstateEvent = services.window.eventFromName("popstate")
  private val pushStateEvent = Evt[Unit]()

  def render(using services: ServicesWithApplication): Signal[VNode] =
    page.map(_.render)

  // Add new route without changing page
  def toStateOnly(state: RoutingState) =
    services.logger.trace(s"Routing silently ${state.silent}")
    services.window.routeTo(state, services.window.routePath)
    pushStateEvent.fire(())

  def to(newPage: Page, state: RoutingState = RoutingState()) =
    services.logger.trace(s"Routing to ${linkPath(newPage)}")
    services.window.routeTo(state, linkPath(newPage))
    pushStateEvent.fire(())
    page.set(newPage)

  def toReplace(newPage: Page, state: RoutingState = RoutingState()) =
    services.logger.trace(s"Routing replace to ${linkPath(newPage)}")
    services.window.routeToInPlace(
      state, 
      linkPath(newPage)
    )
    pushStateEvent.fire(())
    page.set(newPage)

  def link(newPage: Page) =
    URL(linkPath(newPage), window.location.href).toString

  def linkPath(newPage: Page) =
    Routes.toPath(newPage).pathString

  def back =
    services.logger.trace(s"Routing back")
    services.window.routeBack

  def state =
    if services.window.routeState == null then
      RoutingState()
    else
      services.window.routeState[RoutingState]

  private val varStateSignal = Fold(state)(
    popstateEvent.act(_ => state),
    pushStateEvent.act(_ => state)
  )

  def stateSignal =
    varStateSignal

  services.logger.trace(s"Routing initial from ${window.location.pathname} to ${linkPath(page.now)}")

  // Ensure initial path is correctly set
  // Example: for path "/counter" and pattern "counter/{number=0}" the
  //          url should be "/counter/0" and not "/counter"
  services.window.routeToInPlace(RoutingState(false), linkPath(page.now))

  // Change path when url changes by user action
  popstateEvent.observe(_ =>
    if !state.silent then
      page.set(Routes.fromPath(Path(services.window.routePath)))
  )
