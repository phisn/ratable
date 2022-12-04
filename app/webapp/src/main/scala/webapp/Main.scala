package webapp

import cats.effect.SyncIO
import colibri.Observer
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.messages.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.*
import webapp.application.framework.{given, *}
import webapp.application.pages.*
import webapp.services.*
import webapp.state.framework.{given, *}

@main
def main(): Unit =
  implicit val services = ServicesDefault
  Outwatch.renderReplace[SyncIO]("#app", app).unsafeRunSync()

def app(using services: ServicesWithApplication) =
  // Need body wrapper because renderReplace can not directly 
  // take (or I do not know how) a colibri.Source / rescala.Signal
  body(
    // Prevent accidental webpage reload on mobile devices
    // https://stackoverflow.com/questions/52342200/how-to-prevent-pull-to-refresh-in-pwa-progressive-web-apps
    cls := "min-h-screen overscroll-y-contain",
    services.routing.render
  )
