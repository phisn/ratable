package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.*
import core.framework.ecmrdt.*
import core.messages.common.*
import core.messages.http.*
import kofre.base.*
import rescala.default.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import scala.util.*
import webapp.device.services.*
import webapp.services.*
import webapp.state.framework.*

class AggregateFacadeProvider(services: {
  val functionsHttpApi: FunctionsHttpApiInterface
  val logger: LoggerServiceInterface
  val stateStorage: StateStorageService
}):
  val facades = collection.mutable.Map[AggregateGid, AggregateFacade[_, _]]()
  val facadesInLoading = collection.mutable.Map[AggregateGid, Future[Option[AggregateFacade[_, _]]]]()

  def get[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec](gid: AggregateGid): Future[Option[AggregateFacade[A, C]]] =
    facades
      .get(gid).map(x => Future.successful(Some(x)))
      .orElse(facadesInLoading.get(gid)) 
    match
      case Some(value) => value.asInstanceOf[Future[Option[AggregateFacade[A, C]]]]
      case None => getFromStorageAndRegister(gid)

  def getFromStorageAndRegister[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec](gid: AggregateGid): Future[Option[AggregateFacade[A, C]]] =
    val aggregateInFuture = getFromStorage[A, C](gid)
    facadesInLoading(gid) = aggregateInFuture

    aggregateInFuture.andThen(_ => 
      facadesInLoading -= gid
    )

    aggregateInFuture.andThen {
      case Success(Some(value)) => facades(gid) = value
    }

    aggregateInFuture

  def getFromStorage[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec](gid: AggregateGid): Future[Option[AggregateFacade[A, C]]] =
    services.stateStorage.load[A, C](gid).map(_.map(initial =>
      val aggregate = AggregateFacade(initial)

      aggregate.listen.observe(aggregate =>
        services.stateStorage.save(gid, aggregate)
      )

      aggregate
    ))
