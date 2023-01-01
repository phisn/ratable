package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.ecmrdt.*
import core.messages.common.{AggregateGid, AggregateType}
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import scala.util.*
import webapp.state.framework.*

import webapp.services.*
import webapp.device.services.*
import core.messages.socket.ServerSocketMessage.Message.Acknowledge
import core.messages.socket.AcknowledgeEventMessage
import core.framework.Crypt

class AggregateViewRepositoryFactory(services: {
  val aggregateViewProvider: AggregateViewProvider
  val aggregateFacadeProvider: AggregateFacadeProvider
  val functionsSocketApi: FunctionsSocketApiInterface
  val logger: LoggerServiceInterface
  val stateDistribution: StateDistributionService
  val stateStorage: StateStorageService
}):
  def create[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec](aggregateType: AggregateType)(using EffectPipeline[A, C], Crypt): AggregateViewRepository[A, C, E] =
    services.stateStorage.migrateAggregateType(aggregateType)

    services.functionsSocketApi.listen {
      case Acknowledge(message) =>
        services.aggregateFacadeProvider.get[A, C, E](message.gid).andThen {
          case Success(Some(aggregateFacade)) =>
            aggregateFacade.mutateTrivial(_.acknowledge(message.time))
        }
    }

    services.stateDistribution.listenForEvents[A, C, E](aggregateType, (gid, event) =>
      services.aggregateFacadeProvider.get[A, C, E](gid).andThen {
        case Success(Some(aggregateFacade)) =>
          aggregateFacade.mutate(_.effectPrepared(event))
      }
    )

    new AggregateViewRepository:
      def all: Future[Seq[(AggregateGid, A)]] = 
        Future.successful(Seq.empty)
      
      def create(id: String, aggregate: A): Future[AggregateView[A, C, E]] =
        services.aggregateViewProvider.create[A, C, E](AggregateGid(id, aggregateType), aggregate)

      def get(id: String): Future[Option[AggregateView[A, C, E]]] =
        services.aggregateViewProvider.get[A, C, E](AggregateGid(id, aggregateType))
