package webapp.state

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.*
import core.messages.common.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.device.storage.*
import webapp.state.framework.*
import org.scalajs.dom.IDBKeyRange

class StateStorage(db: StorageDatabaseInterface):
  def save[A : JsonValueCodec](gid: AggregateGid, aggregate: DeltaContainer[A]) =
    db.put(gid.aggregateType.name, gid.aggregateId) {
      JsAggregateContainer(
        aggregateJson = writeToString(aggregate),
        tag = aggregate.maxTag.toDouble
      )
    }

  def load[A : JsonValueCodec](gid: AggregateGid): Future[Option[DeltaContainer[A]]] =
    db.get[JsAggregateContainer](gid.aggregateType.name, gid.aggregateId)
      .map(_.map(container => readFromString(container.aggregateJson)))

  def unacknowledged[A : JsonValueCodec](aggregateType: AggregateType): Future[Seq[(AggregateGid, DeltaContainer[A])]] =
    db.all[JsAggregateContainer](aggregateType.name, "tag",
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

  class JsAggregateContainer(
    val aggregateJson: String,

    // Must be double for js compatibility. Long or Int do not work!
    val tag: Double

  ) extends scalajs.js.Object
