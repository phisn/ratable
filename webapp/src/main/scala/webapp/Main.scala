package webapp

import cats.effect.SyncIO
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.*
import webapp.services.*
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*

import scala.util.*

object ServicesProduction extends Services:
  lazy val config = new ApplicationConfig()
  lazy val stateDistribution = new StateDistributionService(this)
  lazy val stateProvider = new StateProviderService(this)

@main
def main(): Unit =
  implicit val services = ServicesProduction
  Outwatch.renderInto[SyncIO]("#app", app).unsafeRunSync()

def app(using services: Services) =
  val clickEvent = Evt[Int]()

  clickEvent.observe(ratingValue => 
    services.stateProvider.ratings(repo => repo.rate(ratingValue))
  )

  div(
    button(onClick.map(_ => Random.between(0, 10)) --> clickEvent),
    ratings
  )
