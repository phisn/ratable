package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.*
import core.framework.ecmrdt.*
import core.messages.common.*
import core.messages.http.*
import kofre.base.*
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
  private val facades = collection.mutable.Map[AggregateGid, AggregateFacade[_, _, _]]()
  private val facadesInLoading = collection.mutable.Map[AggregateGid, Future[Option[AggregateFacade[_, _, _]]]]()

  def create[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec](gid: AggregateGid, initial: A): Future[AggregateFacade[A, C, E]] =
    val aggregate = EventBufferContainer(ECmRDT[A, C, E](initial))

    services.stateStorage.save[A, C, E](gid, aggregate).map(_ =>
      val facade = aggregateToFacade(gid, aggregate)
      facades += (gid -> facade)
      facade
    )

  def get[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec](gid: AggregateGid): Future[Option[AggregateFacade[A, C, E]]] =
    facades
      .get(gid).map(x => Future.successful(Some(x)))
      .orElse(facadesInLoading.get(gid)) 
    match
      case Some(value) => value.asInstanceOf[Future[Option[AggregateFacade[A, C, E]]]]
      case None => getFacadeFromStorage[A, C, E](gid)

  private def getFacadeFromStorage[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec](gid: AggregateGid): Future[Option[AggregateFacade[A, C, E]]] =
    val aggregateInFuture = services.stateStorage.load[A, C, E](gid)
      .map(_.map(aggregateToFacade[A, C, E](gid, _)))
    
    facadesInLoading(gid) = aggregateInFuture

    aggregateInFuture.andThen(_ => 
      facadesInLoading -= gid
    )

    aggregateInFuture.andThen {
      case Success(Some(value)) => facades(gid) = value
    }

    aggregateInFuture

  private def aggregateToFacade[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec](gid: AggregateGid, initial: EventBufferContainer[A, C, E]) =
    val aggregate = AggregateFacade(initial)

    aggregate.listen.observe(aggregate =>
      services.stateStorage.save[A, C, E](gid, aggregate)
    )

    aggregate
