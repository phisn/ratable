package webapp.usecases.ratings

import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.store.framework.given
import webapp.Services
import kofre.decompose.containers.DeltaBufferRDT
import kofre.syntax.ArdtOpsContains

def ratingsNew(ratingValue: Int)(using services: Services): Long =
  val id = services.stateProvider.ratings.now.uniqueID(services.config.replicaID)
  services.stateProvider.ratings(_.insert(id, ratingValue))
  id
