package webapp.state.services

import collection.immutable.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.messages.common.*
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

import core.framework.*
import core.messages.common.*
import core.messages.http.*
import core.messages.socket.*
import typings.std.global.TextEncoder
import scala.scalajs.js.typedarray.Int8Array
import core.framework.TaggedDelta
import scala.util.Failure
import webapp.device.services.*
import core.domain.aggregates.ratable.*
import webapp.device.storage.*
import typings.std.stdStrings.storage

// Abstract all state creation in one place away 
// from the core services into the state module
class ApplicationStateFactory(services: {
  val logger: LoggerServiceInterface
  val functionsHttpApi: FunctionsHttpApiInterface
  val functionsSocketApi: FunctionsSocketApiInterface
  val storage: StorageServiceInterface
}):
  def buildApplicationState =
    val builder = services.storage.openDatabase("state")

    newRepositoryFacadeBuilder[Ratable](AggregateType.Ratable, builder)

  def newRepositoryFacadeBuilder[A : JsonValueCodec : Bottom : Lattice](
    aggregateType: AggregateType, 
    builder: StorageDatabaseBuilderInterface
  ): NewFacadeRepository[A] =
    builder.newMigration { migrator =>
      migrator.store(aggregateType.name)
    }

    newRepositoryFacade(aggregateType, StateStorage(builder.assume))

  def newRepositoryFacade[A : JsonValueCodec : Bottom : Lattice](
    aggregateType: AggregateType, 
    stateStorage: StateStorage
  ): NewFacadeRepository[A] =
    val facades = collection.mutable.Map[String, NewFacade[A]]()

    new NewFacadeRepository:
      def create(id: String, aggregate: A): Future[Unit] =
        facadeFromInitial(AggregateGid(id, aggregateType), DeltaContainer(aggregate)).mutate(_ => aggregate)

      def get(id: String): Future[Option[NewFacade[A]]] =
        facades.get(id) match
          case Some(facade) => Future.successful(Some(facade))
          case None =>
            initialAggregate(AggregateGid(id, aggregateType), stateStorage)
              .map(_.map(aggregate => 
                facadeFromInitial(AggregateGid(id, aggregateType), aggregate)
              ))
              .andThen {
                case Success(Some(facade)) => facades += id -> facade
              }

  def newFacade[A : JsonValueCodec : Bottom : Lattice](
    aggregateType: AggregateType, 
    stateStorage: StateStorage
  ): NewFacade[A] =
    val gid = AggregateGid(aggregateType.name, aggregateType)

    val facade = initialAggregate(gid, stateStorage)
      .map(_.getOrElse(Bottom[DeltaContainer[A]].empty))
      .map(aggregate => facadeFromInitial(gid, aggregate))

    new NewFacade:
      def mutate(f: A => A) = facade.flatMap(_.mutate(f))
      def listen = Signals.fromFuture(facade.map(_.listen)).flatten

  def initialAggregate[A : JsonValueCodec : Bottom : Lattice](
    gid: AggregateGid,
    stateStorage: StateStorage
  ) =
    stateStorage
      .load[A](gid)
      .flatMap {
        case Some(value) => Future.successful(Some(value))
        case None =>
          services.functionsHttpApi.getAggregate(
            GetAggregateMessage(gid)
          )
            .map(_.aggregateJson.map(readFromString[A](_)))
            .map(_.map(DeltaContainer(_)))
      }
      .andThen {
        case Failure(exception) =>
          services.logger.error(s"Failed to load aggregate $gid because ${exception}")
      }

  def facadeFromInitial[A : JsonValueCodec : Bottom : Lattice](gid: AggregateGid, initial: DeltaContainer[A]): NewFacade[A] =
    val actions = Evt[A => A]()

    val deltaEvent = services.functionsSocketApi.listenDeltaMessage
      .map(message => 
      )

    val signal = Events.foldAll(initial) { state => 
      Seq(
        // Actions received from the client are applied directly to the state
        actions.act(state.mutate(_)),
      )
    }

    new NewFacade:
      def mutate(f: A => A): Future[Unit] =
        Future.successful(())

      def listen =
        Signal(Bottom.empty)

    /*
    val (
      deltaEvt,
      deltaAckEvt
    ) = services.stateDistribution.aggregateEventsFor[A](gid)

    val offlineEvent = services.window.eventFromName("offline")

    val signal = Events.foldAll(initial) { state => 
      Seq(
        // Actions received from the client are applied directly to the state
        actions.act(action => state.mutate(action)),

        offlineEvent.act(_ => state.deflateDeltas),

        // Deltas with changes from other clients received from the server
        deltaEvt.act(delta => state.applyDelta(delta)),

        // Delta acks are sent as a response to merged deltas and contain the tag of the merged delta
        deltaAckEvt.act(tag => state.acknowledge(tag)),
      )
    }

    signal.changed.observe { _ =>
      services.statePersistence.saveAggregate(gid, signal.now)
    }
    
    // When the state changes by an action, send the delta to the server
    actions.observe { _ =>
      services.stateDistribution.pushDelta(gid, signal.now.mergedDeltas)
    }

    signal
    */


class FacadeBootstrapService(services: {
  val logger: LoggerServiceInterface
  val deltaDispatcher: DeltaDispatcherService
}):
  def bootstrapFacadeFactory[A : JsonValueCodec : Bottom : Lattice](
    aggregateType: AggregateType, 
    builder: StorageDatabaseBuilderInterface,
    factory: (AggregateType, StateStorage) => A
  ): A =
    builder.newMigration { migrator =>
      migrator.store(aggregateType.name)
    }

    val stateStorage = StateStorage(builder.assume)

    stateStorage.unacknowledged[A](aggregateType)
      .andThen {
        case Success(unacknowledged) =>
          unacknowledged.foreach(services.deltaDispatcher.dispatchDeltaToServer(_))
      }
      
    factory(aggregateType, stateStorage)

class DeltaDispatcherService(services: {
  val logger: LoggerServiceInterface
  val functionsSocketApi: FunctionsSocketApiInterface
}):
  def dispatchDeltaToServer[A : JsonValueCodec : Bottom : Lattice](
    gid: AggregateGid,
    delta: DeltaContainer[A]
  ) =
    services.functionsSocketApi.send(ClientSocketMessage.Message.Delta(
      DeltaMessage(
        gid,
        writeToString(delta.mergedDeltas),
        delta.maxTag
      )
    ))
    .andThen {
      case Failure(exception) =>
        services.logger.error(s"Failed to dispatch delta $delta because ${exception}")
    }
