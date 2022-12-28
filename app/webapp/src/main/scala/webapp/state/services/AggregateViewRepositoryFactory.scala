package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.ecmrdt.*
import core.messages.common.*
import rescala.default.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import scala.util.*
import webapp.state.framework.*

import webapp.services.*
import webapp.device.services.*

class AggregateViewRepositoryFactory(services: {
  val aggregateFacadeProvider: AggregateFacadeProvider
  val logger: LoggerServiceInterface
  val stateDistribution: StateDistributionService
  val stateStorage: StateStorageService
}):
  def create[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec](aggregateType: AggregateType): AggregateViewRepository[A, C] =
    services.stateStorage.registerAggregateType(aggregateType)
    // services.stateDistribution.registerMessageHandler[A, C](aggregateType)

    new AggregateViewRepository:
      def all: Future[Seq[(AggregateGid, A)]] = 
        Future.successful(Seq.empty)
      
      def create(id: String, aggregate: A): Future[AggregateView[A, C]] =
        services.aggregateFacadeProvider.create(AggregateGid(id, aggregateType), aggregate)
          .map(_.view)

      def get(id: String): Future[Option[AggregateView[A, C]]] =
        services.aggregateFacadeProvider.get[A, C](AggregateGid(id, aggregateType))
          .map(_.map(_.view))
      
