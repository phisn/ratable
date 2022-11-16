package webapp.usecases.ratable

import core.state.aggregates.ratable.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.*
import webapp.state.framework.{given, *}

def rateRatable(id: String, ratingForCategory: Map[Int, Int])(using services: Services) =
  services.logger.log(s"Rating ratable with id: $id")
  services.state.ratables.mutate(id, _.rate(ratingForCategory, services.config.replicaID))
