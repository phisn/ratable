package webapp.application.usecases.ratable

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.domain.aggregates.library.*
import core.domain.aggregates.ratable.*
import core.framework.*
import core.framework.ecmrdt.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.*
import webapp.state.framework.{given, *}

import core.domain.aggregates.library.{RatableLibrary, RatableLibraryContext, IndexRatableEvent}
def indexRatable(ratableId: AggregateId, password: Option[String] = None)(using services: Services, crypt: Crypt) =
  for
    replicaId <- services.config.replicaId

    libraryView <- services.state.library.getOrCreate(
      AggregateId.singleton(replicaId), 
      RatableLibrary(replicaId, Map())
    )

    result <- libraryView.listen.now.entries.get(ratableId) match
      case Some(x) if password.isEmpty || password == x.password => 
        services.logger.log(s"Ratable with id $ratableId is already indexed")
        Future.successful(None)

      case _ => 
        libraryView.effect(
          EventWithContext(
            IndexRatableEvent(ratableId, password),
            RatableLibraryContext(replicaId)
          )
        ).value

  yield
    result
