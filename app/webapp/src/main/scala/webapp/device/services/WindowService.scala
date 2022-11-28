package webapp.device.services

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
import webapp.application.{given, *}
import webapp.application.pages.*
import webapp.services.ApplicationConfig

trait WindowServiceInterface:
  def routeState[A <: js.Any]: A
  def routePath: String

  def routeTo(state: js.Any, url: String): Unit
  def routeToInPlace(state: js.Any, url: String): Unit

  def routeBack: Unit

  def isOnline: Boolean

  def eventFromName(name: String): rescala.default.Event[js.Any]
  
class WindowService(services: {
  val config: ApplicationConfig
}) extends WindowServiceInterface:
  def routeState[A <: js.Any] =
    window.history.state.asInstanceOf[A]

  def routePath: String = 
    window.location.pathname

  def routeTo(state: js.Any, url: String) =
    window.history.pushState(state, "", url)

  def routeToInPlace(state: js.Any, url: String) =
    window.history.replaceState(state, "", url)

  def routeBack =
    window.history.back()

  def isOnline =
    window.navigator.onLine

  def eventFromName(name: String) =
    val evt = Evt[js.Any]()
    window.addEventListener(name, (e: js.Any) => evt.fire(e))
    evt
  
  // Change attribute <html data-theme="dark or fantasy"> ...
  services.config.darkMode.map(mode =>
    println(s"Dark mode: ${mode}")
    if mode then
      document.documentElement.setAttribute("data-theme", "dark")
    else
      document.documentElement.setAttribute("data-theme", "fantasy")
  )
