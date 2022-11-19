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

class FacadeBootstrapService(services: {
  val logger: LoggerServiceInterface
  val deltaDispatcher: DeltaDispatcherService
}):
  def bootstrapFacadeFactory[A : JsonValueCodec : Bottom : Lattice, U](
    aggregateType: AggregateType, 
    builder: StorageDatabaseBuilderInterface,
    factory: (AggregateType, StateStorage) => U
  ): U =
    builder.newMigration { migrator =>
      migrator.store(aggregateType.name)
    }

    val stateStorage = StateStorage(builder.assume)

    stateStorage.unacknowledged[A](aggregateType)
      .andThen {
        case Success(unacknowledged) =>
          unacknowledged.foreach { 
            case (gid, aggregate) => services.deltaDispatcher.dispatchToServer(gid, aggregate)
          }
      }

    factory(aggregateType, stateStorage)
