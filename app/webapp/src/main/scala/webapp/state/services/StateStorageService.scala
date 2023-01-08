package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.*
import core.framework.ecmrdt.*
import core.messages.common.*
import kofre.base.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import scala.scalajs.js
import webapp.device.storage.*
import webapp.device.services.*
import webapp.state.framework.*
import org.scalajs.dom.IDBKeyRange

class StateStorageService(services: {
  val storage: StorageServiceInterface
}):
  val builder = services.storage.openDatabase("state", 5)
  val db = builder.assume

  def migrateAggregateType(aggregateType: AggregateType) =
    builder.newMigration(5) { migrator =>
      // Remove old store from previous version
      migrator.remove(aggregateType.name)
      migrator.store(aggregateType.name, Set(IndexKeys.pending))
    }

  def finishAggregateRegistration =
    builder.build

  def save[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec](gid: AggregateGid, aggregate: EventBufferContainer[A, C, E]): Future[Unit] =
    db.put(gid.aggregateType.name, writeToString(gid.aggregateId)) {
      JsAggregateContainer(
        // Funny thing is we can savely deflate before saving because deltas are only infalted
        // because we might expect acknoledgements from the server. 

        aggregate = aggregate.toJs,
        pending = aggregate.events.size.toDouble
      )
    }

  def load[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec](gid: AggregateGid): Future[Option[EventBufferContainer[A, C, E]]] =
    db.get[JsAggregateContainer](gid.aggregateType.name, writeToString(gid.aggregateId))
      .map(_.map(container => container.aggregate.toScala))

  /*
  def unacknowledged[A : JsonValueCodec](aggregateType: AggregateType): Future[Seq[(AggregateGid, DeltaContainer[A])]] =
    db.all[JsAggregateContainer](aggregateType.name, IndexKeys.pending,
      // Lowerbound is closed from 1. This means the first number will be 1.  
      IDBKeyRange.lowerBound(1, open = false)
    )
      .map(_.map {
        case (aggregateId, container) => 
          (
            AggregateGid(aggregateId, aggregateType), 
            container.aggregate.toScala
          )
      })

  def all[A : JsonValueCodec](aggregateType: AggregateType): Future[Seq[(AggregateGid, DeltaContainer[A])]] =
    db.all[JsAggregateContainer](aggregateType.name, IndexKeys.id)
      .map(_.map {
        case (aggregateId, container) => 
          (
            AggregateGid(aggregateId, aggregateType), 
            container.aggregate.toScala
          )
      })

  */
  
  object IndexKeys:
    val pending = "pending"
    val id = "id"
    
  class JsAggregateContainer(
    val aggregate: js.Any,

    // Must be double for js compatibility. Long or Int do not work!
    val pending: Double

  ) extends scalajs.js.Object
