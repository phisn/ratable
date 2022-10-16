package webapp.usecases.ratable

import webapp.*
import webapp.store.framework.{given, *}
import webapp.store.aggregates.ratable.create

def rateRatable(id: String, ratingForCategory: Map[Int, Int])(using services: Services) =
  services.stateProvider.ratables(_.mutate(id, _.rate(ratingForCategory, services.config.replicaID)))
