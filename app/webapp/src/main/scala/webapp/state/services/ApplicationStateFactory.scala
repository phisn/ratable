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
  val storage: StorageServiceInterface
  val deltaDispatcher: DeltaDispatcherService
  val facadeFactory: FacadeFactory
}):
  def buildApplicationState: ApplicationState =
    val builder = services.storage.openDatabase("state")

    val state = ApplicationState(
      newFacadeRepositoryHelper[Ratable](AggregateType.Ratable, builder)
    )

    builder.build
    state

  def newFacadeHelper[A : JsonValueCodec : Bottom : Lattice](aggregateType: AggregateType, builder: StorageDatabaseBuilderInterface): Facade[A] =
    bootstrapFacadeFactory(aggregateType, builder, services.facadeFactory.Facade[A])

  def newFacadeRepositoryHelper[A : JsonValueCodec : Bottom : Lattice](aggregateType: AggregateType, builder: StorageDatabaseBuilderInterface): FacadeRepository[A] =
    bootstrapFacadeFactory(aggregateType, builder, services.facadeFactory.FacadeRepository[A])

  def bootstrapFacadeFactory[A : JsonValueCodec : Bottom : Lattice, U](
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
              services.deltaDispatcher.dispatchToServer(gid, aggregate.mergedDeltas)
          }
      }

    factory(aggregateType, stateStorage)
