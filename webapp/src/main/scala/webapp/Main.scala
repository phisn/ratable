package webapp

import cats.effect.SyncIO
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.pages.*
import webapp.services.*
import webapp.store.framework.{given, *}

import webapp.store.given
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import colibri.Observer

object ServicesProduction extends Services:
  val jsBootstrap = JSBootstrapService()
  lazy val config = ApplicationConfig()
  lazy val stateDistribution = StateDistributionService(this)
  lazy val stateProvider = StateProviderService(this)
  lazy val backendApi = BackendApiService(this)
  lazy val routing = RoutingService()

@main
def main(): Unit =
  implicit val services = ServicesProduction
  Outwatch.renderReplace[SyncIO]("#app", app).unsafeRunSync()

def app(using services: Services) =
  body(
    cls := "min-h-screen",
    services.routing.render
  ) 