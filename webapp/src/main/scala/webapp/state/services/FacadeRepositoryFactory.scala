package webapp.state.services

import collection.immutable.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import kofre.base.*
import kofre.decompose.containers.*
import rescala.default.*
import rescala.operator.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import webapp.services.*
import webapp.state.framework.{*, given}
import webapp.state.{*, given}
import scala.util.Success

class FacadeRepositoryFactory(services: {
  val logger: LoggerServiceInterface
  val statePersistence: StatePersistenceServiceInterface
  val aggregateFactory: AggregateFactory
}):
  def registerAggregateAsRepository[A : JsonValueCodec : Bottom : Lattice](
    aggregateTypeId: String
  ): FacadeRepository[A] = 
    services.statePersistence.migrationForRepository(aggregateTypeId)

    val facades = collection.mutable.Map[String, Facade[A]]()

    def tryLoadAggregate(id: String) =
      val actions = Evt[A => A]()
        
      val facadeInFuture = services.statePersistence
        .loadAggregate(aggregateTypeId, id)
        .map(_.map(aggregate =>
          Facade(
            actions,
            services.aggregateFactory.createAggregateSignal(actions)(aggregate),
          )
        ))

      actions.recoverEventsUntilCompleted(facadeInFuture)

      facadeInFuture
        .andThen {
          case Success(Some(facade)) => facades.put(id, facade)
          case _ =>
        }
  
    new FacadeRepository:
      def get(id: String): Future[Option[Facade[A]]] =
        facades.get(id) match
          case Some(facade) => Future.successful(Some(facade))
          case None => tryLoadAggregate(id)

      def create(id: String, aggregate: A): Unit =
        val actions = Evt[A => A]()

        val facade = Facade(
          actions, 
          services.aggregateFactory.createAggregateSignal(actions)(DeltaContainer(aggregate))
        )

        facades += id -> facade

        // Explicitly save the aggregate, because saving is usally done by action handling
        services.statePersistence.saveAggregate(aggregateTypeId, id, DeltaContainer(aggregate))

