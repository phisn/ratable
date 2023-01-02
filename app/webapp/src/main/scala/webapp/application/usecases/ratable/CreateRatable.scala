package webapp.application.usecases.ratable

import core.domain.aggregates.ratable.ecmrdt.*
import core.framework.{given, *}
import webapp.*
import webapp.state.framework.{given, *}
import core.framework.ecmrdt.Claim

import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

def createRatable(title: String, categories: List[String])(using services: Services, crypt: Crypt) =
  services.logger.log(s"Creating ratable with title: $title")

  val id = services.state.uniqueID

  for
    (claims, provers) <- Claim.create(RatableClaims.values.toSet)
  do
    services.state.ratables.create(id, Ratable(claims, title, categories))

  id

  /*
  services.state.ratables.create(id, Ratable(title, categories, services.config.replicaID))
  */
