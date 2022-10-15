package webapp.usecases.rating

import webapp.store.aggregates.rating.*
import webapp.store.framework.*
import webapp.store.framework.given
import webapp.Services
import kofre.decompose.containers.DeltaBufferRDT
import kofre.syntax.ArdtOpsContains

def ratingsNew(ratingValue: Int)(using services: Services): String =
  val id = services.stateProvider.ratings.now.uniqueID(services.config.replicaID)
  services.stateProvider.ratings(_.create(id, ratingValue, services.config.replicaID))
  id
