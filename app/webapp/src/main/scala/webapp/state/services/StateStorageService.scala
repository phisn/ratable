package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.*
import core.messages.common.*
import kofre.base.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import webapp.device.storage.*
import webapp.device.services.*
import webapp.state.framework.*
import org.scalajs.dom.IDBKeyRange

class StateStorageService(services: {
  val storage: StorageServiceInterface
}):
  val builder = services.storage.openDatabase("state", 2)
  val db = builder.assume

  def registerAggregateType(aggregateType: AggregateType) =
    builder.newMigration(2) { migrator =>
      // Remove old store from previous version
      migrator.remove(aggregateType.name)

      migrator.store(aggregateType.name, Set(IndexKeys.tag))
    }

  def finishAggregateRegistration =
    builder.build

  def save[A : JsonValueCodec : Lattice : Bottom](gid: AggregateGid, aggregate: DeltaContainer[A]) =
    db.put(gid.aggregateType.name, gid.aggregateId) {
      JsAggregateContainer(
        // Funny thing is we can savely deflate before saving because deltas are only infalted
        // because we might expect acknoledgements from the server. 
        aggregateJson = writeToString(aggregate.deflateDeltas),
        tag = aggregate.maxTag.toDouble
      )
    }

  def load[A : JsonValueCodec](gid: AggregateGid): Future[Option[DeltaContainer[A]]] =
    db.get[JsAggregateContainer](gid.aggregateType.name, gid.aggregateId)
      .map(_.map(container => readFromString(container.aggregateJson)))

  def unacknowledged[A : JsonValueCodec](aggregateType: AggregateType): Future[Seq[(AggregateGid, DeltaContainer[A])]] =
    db.all[JsAggregateContainer](aggregateType.name, IndexKeys.tag,
      // Lowerbound is closed from 1. This means the first number will be 1.  
      IDBKeyRange.lowerBound(1, open = false)
    )
      .map(_.map {
        case (aggregateId, container) => 
          (
            AggregateGid(aggregateId, aggregateType), 
            readFromString(container.aggregateJson)
          )
      })
  
  object IndexKeys:
    val tag = "tag"
    
  class JsAggregateContainer(
    val aggregateJson: String,

    // Must be double for js compatibility. Long or Int do not work!
    val tag: Double

  ) extends scalajs.js.Object
