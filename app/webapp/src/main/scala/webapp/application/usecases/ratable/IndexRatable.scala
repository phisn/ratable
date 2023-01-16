package webapp.application.usecases.ratable

import cats.data.*
import cats.implicits.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.domain.aggregates.library.*
import core.domain.aggregates.ratable.*
import core.framework.*
import core.framework.ecmrdt.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.*
import webapp.state.framework.{given, *}

def indexRatable(ratableId: AggregateId, password: Option[String] = None)(using services: Services, crypt: Crypt) =
  for
    replicaId <- EitherT.liftF(services.config.replicaId)

    libraryView <- services.state.library.singleton(replicaId)

    _ <- libraryView.listen.now.entries.get(ratableId) match
      case Some(x) if password.isEmpty || password == x.password => 
        services.logger.log(s"Ratable with id $ratableId is already indexed")
        EitherT.pure[Future, RatableError](())

      case _ => 
        libraryView.effect(
          IndexRatableEvent(ratableId, password),
          RatableLibraryContext(replicaId)
        )

  yield
    ()
