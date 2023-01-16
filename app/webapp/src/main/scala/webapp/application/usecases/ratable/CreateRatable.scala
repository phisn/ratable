package webapp.application.usecases.ratable

import cats.data.*
import cats.implicits.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.domain.aggregates.library.*
import core.domain.aggregates.ratable.*
import core.framework.{given, *}
import core.framework.ecmrdt.*
import webapp.*
import webapp.state.framework.{given, *}
import core.framework.ecmrdt.Claim

import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import core.domain.aggregates.ratable.Ratable
import core.domain.aggregates.library.{RatableLibrary, RatableLibraryContext, IndexRatableEvent}

def createRatable(title: String, categories: List[String])(using services: Services, crypt: Crypt) =
  services.logger.log(s"Creating ratable with title: $title")

  for
    replicaId <- EitherT.liftF(services.config.replicaId)
    aggregateId = AggregateId.unique(replicaId)

    library <- services.state.library.singleton(replicaId)

    createResult <- EitherT.liftF(
      core.domain.aggregates.ratable.createRatable(title, categories)
    )

    ratableView = services.state.ratables.create(aggregateId)

    _ = services.logger.log(s"1 Created ratable with id: $aggregateId")

    _ <- ratableView.effect(
      createResult.event, 
      RatableContext(replicaId, List())
    )

    _ = services.logger.log(s"2 Created ratable with id: $aggregateId")

    _ <- library.effect(
      IndexRatableEvent(aggregateId, Some(createResult.password)),
      RatableLibraryContext(replicaId)
    )

  yield
    aggregateId
