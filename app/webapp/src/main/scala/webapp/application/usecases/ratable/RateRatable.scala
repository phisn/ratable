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

def rateRatable(id: AggregateId, ratingForCategory: Map[Int, Int])(using services: Services, crypt: Crypt) =
  services.logger.log(s"Rating ratable with id: $id")

  for
    replicaId <- EitherT.liftF(services.config.replicaId)

    ratable <- services.state.ratables.getEnsure(id)

    library <- services.state.library.getEnsure(AggregateId.singleton(replicaId))

    libraryEntry <- EitherT.fromOption(
      library.listen.now.entries.get(id),
      RatableError("Ratable is not indexed")
    )

    password <- EitherT.fromOption(
      libraryEntry.password,
      RatableError("Ratable is not indexed with password")
    )

    claimProof <- ratable.listen.now.proveByPassword(
      replicaId,
      RatableClaims.CanRate, 
      password
    )

    _ <- ratable.effect(
      EventWithContext(
        RateEvent(ratingForCategory),
        RatableContext(replicaId, List(claimProof))
      )
    )

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
