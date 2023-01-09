package webapp.application.usecases.ratable

import cats.data.*
import cats.implicits.*
import core.domain.aggregates.ratable.*
import core.domain.aggregates.ratable.*
import core.framework.*
import core.framework.ecmrdt.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.*
import webapp.state.framework.{given, *}
import core.domain.aggregates.ratable.{RatableClaims, RatableContext, RateEvent}

def rateRatable(id: AggregateId, password: String, ratingForCategory: Map[Int, Int])(using services: Services, crypt: Crypt) =
  services.logger.log(s"Rating ratable with id: $id")

  for
    replicaId <- EitherT.liftF(services.config.replicaId)

    ratable <- services.state.ratables.getEnsure(id)

    claimProof <- ratable.listen.now.proveByPassword(
      replicaId,
      RatableClaims.CanRate, 
      password
    )

    /*
    claimProof <- ratable match
      case Left(x) => Future.successful(Left(x))
      case Right(ratable) => 
        ratable.listen.now.proveByPassword(
          replicaId,
          RatableClaims.CanRate, 
          password
        )
        .map(_.toRight("Password is incorrect"))

    result <- claimProof match
      case Left(x) => Future.successful(Left(x))
      case Right(claimProof) => 
        ratable.effect(
          EventWithContext(
            RateEvent(ratingForCategory),
            RatableContext(replicaId, Set(claimProof))
          )
        )
    */

  yield
    ()

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
