package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import collection.immutable.*
import core.framework.*
import core.messages.common.*
import core.messages.http.*
import core.messages.socket.*
import core.domain.aggregates.ratable.ecmrdt.*
import kofre.base.*
import rescala.default.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import scala.util.*
import webapp.services.*
import webapp.device.services.*
import webapp.state.*
import webapp.state.framework.*

// Abstract all state creation in one place away 
// from the core services into the state module
class ApplicationStateFactory(services: {
  val aggregateFacadeProvider: AggregateFacadeProvider
  val logger: LoggerServiceInterface
  val stateDistribution: StateDistributionService
  val stateStorage: StateStorageService
}):
  def buildApplicationState: ApplicationState = ???
  /*
    val state = ApplicationState(
      registerAggregateRepository[Ratable](AggregateType.Ratable)
    )

    services.stateStorage.finishAggregateRegistration

    state
  
  def registerAggregateRepository[A : JsonValueCodec : Bottom : Lattice](aggregateType: AggregateType): AggregateViewRepository[A] =
    services.stateStorage.registerAggregateType(aggregateType)
    services.stateDistribution.registerMessageHandler[A](aggregateType)

    distributeUnacknowledged[A](aggregateType)

    newAggregateRepository[A](aggregateType)

  def newAggregateRepository[A : JsonValueCodec : Bottom : Lattice](aggregateType: AggregateType): AggregateViewRepository[A] =
    new AggregateViewRepository:
      def all: Future[Seq[(AggregateGid, A)]] =
        services.stateStorage.all(aggregateType)
          .map(_.map((a, b) => (a, b.inner)))

      def create(id: String, aggregate: A): AggregateView[A] =
        services.aggregateFacadeProvider
          .fromInitial(AggregateGid(id, aggregateType), aggregate)
          .toView

      def get(id: String): Future[Option[AggregateView[A]]] =
        services.aggregateFacadeProvider
          .get(AggregateGid(id, aggregateType))
          .map(_.map(_.toView))
          .andThen {
            case Success(None) =>
              services.logger.error(s"ApplicationStateFactory: No aggregate facade for $id")

            case Failure(exception) =>
              services.logger.error(s"ApplicationStateFactory: Error getting aggregate facade for $id with $exception")
          }

  def distributeUnacknowledged[A : JsonValueCodec : Bottom : Lattice](aggregateType: AggregateType) =
    services.stateStorage.unacknowledged[A](aggregateType)
      .andThen {
        case Success(unacknowledged) =>
          unacknowledged.foreach { 
            case (gid, aggregate) => 
              services.logger.trace(s"Unacknowledged ${aggregateType.name} ${gid} found")
              services.stateDistribution.dispatchToServer(gid, aggregate.mergedDeltas)
          }
      }
  */
  /*
  def newAggregateViewHelper[A : JsonValueCodec : Bottom : Lattice](aggregateType: AggregateType, builder: StorageDatabaseBuilderInterface): AggregateView[A] =
    bootstrapAggregateViewFactory(aggregateType, builder, services.facadeFactory.AggregateView[A])

  def newAggregateViewRepositoryHelper[A : JsonValueCodec : Bottom : Lattice](aggregateType: AggregateType, builder: StorageDatabaseBuilderInterface): AggregateViewRepository[A] =
    bootstrapAggregateViewFactory(aggregateType, builder, services.facadeFactory.AggregateViewRepository[A])

  def bootstrapAggregateViewFactory[A : JsonValueCodec : Bottom : Lattice, U](
    aggregateType: AggregateType, 
    builder: StorageDatabaseBuilderInterface,
    factory: (AggregateType, StateStorage) => U
  ): U =
    builder.newMigration { migrator =>
      migrator.store(aggregateType.name, Set("tag"))
    }

    val stateStorage = StateStorage(builder.assume)

    /**
      * We need some reversement of control here. Currently when the server sends a message about a aggregate we fire a event.
      * If the aggregate behind this event is not yet loaded we currently do not react to this event. We should instead load the 
      * aggregate and then fire the event.
      */

    stateStorage.unacknowledged[A](aggregateType)
      .andThen {
        case Success(unacknowledged) =>
          unacknowledged.foreach { 
            case (gid, aggregate) => 
              services.logger.trace(s"Unacknowledged ${aggregateType.name} ${gid} found")
              services.stateDistribution.dispatchToServer(gid, aggregate.mergedDeltas)
          }
      }

    factory(aggregateType, stateStorage)
  */
