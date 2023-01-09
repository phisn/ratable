package webapp.application.usecases.ratable

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
    replicaId <- services.config.replicaId

    library <- services.state.library.getOrCreate(
      AggregateId.singleton(replicaId), 
      RatableLibrary(replicaId, Map())
    )

    aggregateId = AggregateId.unique(replicaId)

    _ = services.logger.log(s"Creating aggregate with id: $aggregateId")

    (ratable, password) <- Ratable(
      title,
      categories
    )

    _ = services.logger.log(s"Created ratable with id: $aggregateId and password $password")

    ratableView <- services.state.ratables.create(
      aggregateId, 
      ratable
    )

    _ = services.logger.log(s"Created ratable with id: $aggregateId and password $password")

    _ <- library.effect(
      EventWithContext(
        IndexRatableEvent(aggregateId, Some(password)),
        RatableLibraryContext(replicaId)
      )
    ).value

  yield
    aggregateId
