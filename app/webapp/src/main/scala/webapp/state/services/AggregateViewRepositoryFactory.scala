package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.ecmrdt.*
import core.messages.common.*
import rescala.default.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
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
  def create[A : JsonValueCodec, C : JsonValueCodec]: AggregateViewRepository[A, C] =
    new AggregateViewRepository:
      def all: Future[Seq[(AggregateGid, A)]] = ???
      
      def create(id: String, aggregate: A): AggregateView[A, C] =
        ???

      def get(id: String): Future[Option[AggregateView[A, C]]] =
        ???
      
