package webapp.state

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.*
import core.messages.common.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.device.storage.*
import webapp.state.framework.*

class StateStorage(db: StorageDatabaseInterface):
  def save[A : JsonValueCodec](gid: AggregateGid, aggregate: DeltaContainer[A]) =
    db.put(gid.aggregateType.name, gid.aggregateId) {
      JsAggregateContainer(
        aggregateJson = writeToString(aggregate),
        tag = aggregate.maxTag
      )
    }

  def load[A : JsonValueCodec](gid: AggregateGid): Future[Option[DeltaContainer[A]]] =
    db.get[JsAggregateContainer](gid.aggregateType.name, gid.aggregateId)
      .map(_.map(container => readFromString(container.aggregateJson)))

  def unacknowledged[A : JsonValueCodec](aggregateType: AggregateType): Future[Seq[(AggregateGid, DeltaContainer[A])]] =
    Future.successful(Seq.empty)

  class JsAggregateContainer(
    val aggregateJson: String,
    val tag: Tag

  ) extends scalajs.js.Object
