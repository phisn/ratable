package webapp.usecases.ratable

import core.store.aggregates.ratable.*
import core.store.framework.{given, *}
import webapp.*
import webapp.store.framework.{given, *}

def createRatable(title: String, categories: List[String])(using services: Services) =
  val id = services.stateProvider.ratables.now.uniqueID(services.config.replicaID)
  services.stateProvider.ratables(_.create(id, title, categories, services.config.replicaID))
  id
