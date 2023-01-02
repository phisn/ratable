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

import webapp.device.framework.given
import scala.util.Success
import scala.util.Failure
import scala.concurrent.ExecutionContext.Implicits.global

@main
def main(): Unit =
  implicit val services = ServicesDefault

  services.config.replicaId.andThen {
    case Success(value) => 
      services.logger.log(s"Replica ID: ${value.publicKey.length}")
    case Failure(exception) => 
      services.logger.error(s"Failed to load replica ID: ${exception}")
  }

  Outwatch.renderReplace[SyncIO]("#app", app).unsafeRunSync()

def app(using services: ServicesWithApplication) =
  // Need body wrapper because renderReplace can not directly 
  // take (or I do not know how) a colibri.Source / rescala.Signal
  body(
    cls := "min-h-screen overscroll-y-contain",
    services.routing.render
  )
