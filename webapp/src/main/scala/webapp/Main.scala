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
import webapp.services.state.*
import webapp.store.given
import webapp.store.framework.{given, *}

object ServicesProduction extends Services:
  lazy val backendApi = BackendApiService(this)
  lazy val config = ApplicationConfig()

  val jsBootstrap = JSBootstrapService()

  lazy val facadeFactory = FacadeFactory(this)
  lazy val stateDistribution = StateDistributionService(this)
  lazy val statePersistence = StatePersistenceService(this)
  val state = StateProvider(this)

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