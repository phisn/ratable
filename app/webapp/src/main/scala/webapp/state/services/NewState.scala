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

import core.messages.common.*
import core.messages.http.*
import typings.std.global.TextEncoder
import scala.scalajs.js.typedarray.Int8Array
import core.framework.TaggedDelta
import scala.util.Failure
import webapp.device.services.*
import core.domain.aggregates.ratable.*
import webapp.device.storage.*

class NewState(services: {
  val storage: StorageServiceInterface
}):
  def buildApplicationState =
    val builder = services.storage.openDatabase("state")

    newRepositoryAggregate[Ratable](AggregateType.Ratable, builder)

  def newRepositoryAggregate[A : JsonValueCodec : Bottom : Lattice](
    aggregateType: AggregateType, 
    builder: StorageDatabaseBuilderInterface
  ): FacadeRepository[A] =
    builder.newMigration { migrator =>
      migrator.store(aggregateType.name)
    }

    new FacadeRepository:
      def create(id: String, aggregate: A): Future[Unit] =
        Future.successful(())

      def get(id: String): Future[Option[Facade[A]]] =
        Future.successful(None)
