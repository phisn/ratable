package webapp.state.services

import cats.data.*
import cats.effect.*
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
  private val facadesInLoading = collection.mutable.Map[AggregateGid, EitherT[Future, RatableError, Option[AggregateFacade[_, _, _]]]]()

  def create[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec](gid: AggregateGid, initial: A) =
    val aggregate = EventBufferContainer(ECmRDT[A, C, E](initial))

    services.stateStorage.save[A, C, E](gid, aggregate).map(_ =>
      val facade = aggregateToFacade(gid, aggregate)
      facades += (gid -> facade)
      facade
    )

  def get[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec](gid: AggregateGid): EitherT[Future, RatableError, Option[AggregateFacade[A, C, E]]] =
    facades
      .get(gid).map(x => EitherT.pure[Future, RatableError](Some(x)))
      .orElse(facadesInLoading.get(gid)) 
    match
      case Some(value) => value.map(_.map(_.asInstanceOf[AggregateFacade[A, C, E]]))
      case None => getFacadeFromStorage[A, C, E](gid)

  private def getFacadeFromStorage[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec](gid: AggregateGid): EitherT[Future, RatableError, Option[AggregateFacade[A, C, E]]] =
    val aggregateInFuture = services.stateStorage.load[A, C, E](gid)
      .map(_.map(aggregateToFacade[A, C, E](gid, _)))
    
    facadesInLoading(gid) = aggregateInFuture.map(_.map(_.asInstanceOf[AggregateFacade[_, _, _]]))

    aggregateInFuture.value.andThen(_ => 
      facadesInLoading -= gid
    )

    aggregateInFuture.value.andThen {
      case Success(Right(Some(value))) => facades(gid) = value
    }

    aggregateInFuture

  private def aggregateToFacade[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec](gid: AggregateGid, initial: EventBufferContainer[A, C, E]) =
    val aggregate = AggregateFacade(initial)

    aggregate.listen.observe(aggregate =>
      services.stateStorage.save[A, C, E](gid, aggregate)
    )

    aggregate
