package webapp

import cats.effect.SyncIO
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.pages.*
import webapp.services.*
import webapp.store.aggregates.ratings.*
import webapp.store.framework.{given, *}
import webapp.usecases.ratings.*

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
  Outwatch.renderInto[SyncIO]("#app", app).unsafeRunSync()

def app(using services: Services) =
  div(
    cls := "flex flex-col min-h-screen bg-base-200",
    div(
      cls := "flex-grow flex flex-col",
      services.routing.render
    ),
    webapp.components.footerComponent
  )
