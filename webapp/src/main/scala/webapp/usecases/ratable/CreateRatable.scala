package webapp.usecases.ratable

import webapp.*
import webapp.store.framework.{given, *}
import webapp.store.aggregates.ratable.create

def createRatable(title: String, categories: List[String])(using services: Services) =
  val id = services.stateProvider.ratables.now.uniqueID(services.config.replicaID)
  services.stateProvider.ratables(_.create(id, title, categories, services.config.replicaID))
  id
