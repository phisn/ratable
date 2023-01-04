package webapp.application.usecases.ratable

import core.domain.aggregates.ratable.ecmrdt.*
import core.framework.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.*
import webapp.state.framework.{given, *}
import core.framework.ecmrdt.EventWithContext

def rateRatable(id: String, password: String, ratingForCategory: Map[Int, Int])(using services: Services, crypt: Crypt) =
  services.logger.log(s"Rating ratable with id: $id")

  for
    replicaId <- services.config.replicaId
    ratable <- services.state.ratables.get(id)

    claimProof <- ratable match
      case None => Future.successful(None)
      case Some(ratable) => 
        ratable.listen.now.proveByPassword(
          replicaId,
          RatableClaims.CanRate, 
          password
        )

    result <- (ratable, claimProof) match
      case (Some(ratable), Some(claimProof)) =>
        ratable.effect(
          EventWithContext(
            RateEvent(ratingForCategory),
            RatableContext(replicaId, Set(claimProof))
          )
        )
        
      case _ => Future.successful(Some("Ratable not found or password is incorrect"))

  yield
    result

/*
  for
    replicaId <- services.config.replicaId
  yield
    services.state.ratables.effect(
      id,
      rateEvent(replicaId, ratingForCategory)
    )
*/
  // services.state.ratables.mutate(id, _.rate(ratingForCategory, services.config.replicaID))
