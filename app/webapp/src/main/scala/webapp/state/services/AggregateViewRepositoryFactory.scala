package webapp.state.services

import cats.data.*
import cats.implicits.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.*
import core.framework.ecmrdt.*
import core.messages.common.*
import core.messages.socket.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import scala.util.*
import webapp.device.services.*
import webapp.services.*
import webapp.state.framework.*

class AggregateViewRepositoryFactory(services: {
  val aggregateViewProvider: AggregateViewProvider
  val aggregateFacadeProvider: AggregateFacadeProvider
  val functionsSocketApi: FunctionsSocketApiInterface
  val logger: LoggerServiceInterface
  val stateDistribution: StateDistributionService
  val stateStorage: StateStorageService
}):
  def create[A : InitialECmRDT : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec]
    (aggregateType: AggregateType)(using EffectPipeline[A, C], Crypt): AggregateViewRepository[A, C, E] =
    services.stateStorage.migrateAggregateType(aggregateType)

    services.functionsSocketApi.listen {
      case ServerSocketMessage.Message.Acknowledge(message) =>
        services.aggregateFacadeProvider.get[A, C, E](message.gid).value.andThen {
          case Success(Right(Some(aggregateFacade))) =>
            aggregateFacade.mutateTrivial(_.acknowledge(message.time))
        }
    }

    services.stateDistribution.listenForEvents[A, C, E](aggregateType, (gid, event) =>
      services.aggregateFacadeProvider.get[A, C, E](gid).value.andThen {
        case Success(Right(Some(aggregateFacade))) =>
          aggregateFacade.mutate(_.effectPrepared(
            event, 
            MetaContext(
              gid.aggregateId,
              gid.aggregateId.replicaId
            )
          ))
      }
    )

    new AggregateViewRepository:
      def all: Future[Seq[(AggregateGid, A)]] = 
        Future.successful(Seq.empty)
      
      def create(id: AggregateId): AggregateView[A, C, E] =
        services.aggregateViewProvider.create[A, C, E](AggregateGid(id, aggregateType))

      def get(id: AggregateId): EitherT[Future, RatableError, Option[AggregateView[A, C, E]]] =
        services.aggregateViewProvider.get[A, C, E](AggregateGid(id, aggregateType))
