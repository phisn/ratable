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
import webapp.pages.*
import webapp.services.*
import webapp.state.framework.{given, *}

@main
def main(): Unit =
  implicit val services = ServicesDefault
  Outwatch.renderReplace[SyncIO]("#app", app).unsafeRunSync()

def app(using services: Services) =
  // Need body wrapper because renderReplace can not directly 
  // take (or I do not know how) a colibri.Source / rescala.Signal
  body(
    cls := "min-h-screen",
    services.routing.render
  )
