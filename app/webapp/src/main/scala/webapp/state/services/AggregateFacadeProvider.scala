package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.*
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
  val aggregateFactory: AggregateFactory
  val functionsHttpApi: FunctionsHttpApiInterface
  val logger: LoggerServiceInterface
  val stateStorage: StateStorageService
}):
  val facades = collection.mutable.Map[AggregateGid, AggregateFacade[_]]()
  val facadesInLoading = collection.mutable.Map[AggregateGid, Future[AggregateFacade[_]]]()

  def get[A : JsonValueCodec : Bottom : Lattice](gid: AggregateGid): Future[Option[AggregateFacade[A]]] =
    facades.get(gid) match
      case Some(value) => Future.successful(Some(value.asInstanceOf[AggregateFacade[A]]))
      case None =>
        initialAggregate(gid)
          .map(_.map(services.aggregateFactory.createSignal(gid, _)))
          .andThen {
            case Success(Some(facade)) =>
              facades += gid -> facade
              facadesInLoading -= gid 
          }
  
  def fromInitial[A : JsonValueCodec : Bottom : Lattice](gid: AggregateGid, initial: A): AggregateFacade[A] =
    val facade = services.aggregateFactory.createSignal(gid, Bottom[DeltaContainer[A]].empty)
    facades += gid -> facade

    // Firing initial value as custom mutation to trigger all important side effects
    facade.mutationEvent.fire(_ => initial)

    facade

  // There does not seem to be any usecase for this method
  /*
  def getWithInitial[A : JsonValueCodec : Bottom : Lattice](gid: AggregateGid, initial: DeltaContainer[A]): Future[AggregateFacade[A]] =
    facades.getOrElseUpdate(
      gid,
      services.aggregateFactory.createSignal(gid, initial)
    ).asInstanceOf[Future[AggregateFacade[A]]]
  */

  private def initialAggregate[A : JsonValueCodec : Bottom : Lattice](
    gid: AggregateGid
  ) =
    services.stateStorage
      .load[A](gid)
      .andThen {
        case Success(Some(_)) =>
          services.logger.trace(s"AggregateViewFactory.initialAggregate: $gid found in storage")

        case Success(None) => 
          services.logger.trace(s"AggregateViewFactory.initialAggregate: $gid not found")
      }
      .flatMap {
        case Some(value) => 
          Future.successful(Some(value))
        
        case None =>
          services.functionsHttpApi.getAggregate(
            GetAggregateMessage(gid)
          )
            .map(_.aggregateJson.map(readFromString[A](_)))
            .map(_.map(DeltaContainer(_)))
            .fallbackTo(Future.successful(None))
      }
      .andThen {
        case Failure(exception) =>
          services.logger.error(s"Failed to load aggregate $gid because ${exception}")
      }

