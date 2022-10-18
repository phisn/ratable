package webapp.usecases.ratable

import core.store.aggregates.ratable.*
import webapp.*
import webapp.store.framework.{given, *}

def rateRatable(id: String, ratingForCategory: Map[Int, Int])(using services: Services) =
  services.stateProvider.ratables(_.mutate(id, _.rate(ratingForCategory, services.config.replicaID)))
