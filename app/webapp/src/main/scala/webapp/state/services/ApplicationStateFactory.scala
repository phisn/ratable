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
  val storage: StorageServiceInterface
  val facadeBootstrap: FacadeBootstrapService
  val facadeFactory: FacadeFactory
}):
  def buildApplicationState: ApplicationState =
    val builder = services.storage.openDatabase("state")

    val state = ApplicationState(
      FacadeRepositoryHelper[Ratable](AggregateType.Ratable, builder)
    )

    builder.build
    state

  def FacadeHelper[A : JsonValueCodec : Bottom : Lattice](aggregateType: AggregateType, builder: StorageDatabaseBuilderInterface): Facade[A] =
    services.facadeBootstrap.bootstrapFacadeFactory(aggregateType, builder, services.facadeFactory.Facade[A])

  def FacadeRepositoryHelper[A : JsonValueCodec : Bottom : Lattice](aggregateType: AggregateType, builder: StorageDatabaseBuilderInterface): FacadeRepository[A] =
    services.facadeBootstrap.bootstrapFacadeFactory(aggregateType, builder, services.facadeFactory.FacadeRepository[A])

