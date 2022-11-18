package webapp.mocks

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.messages.common.*
import core.framework.*
import rescala.default.*

import scala.collection.mutable.Map
import webapp.*
import webapp.state.services.StateDistributionServiceInterface

class StateDistributionServiceMock extends StateDistributionServiceInterface:
  val eventRouter = Map[AggregateGid, EventRouterEntry]()

  case class EventRouterEntry(
    deltaEvent: Evt[Any],
    deltaAckEvent: Evt[Tag]
  )

  override def aggregateEventsFor[A : JsonValueCodec](gid: AggregateGid) =
    val entry = EventRouterEntry(
      deltaEvent = Evt[Any](),
      deltaAckEvent = Evt[Tag]()
    )

    eventRouter(gid) = entry
    
    (
      entry.deltaEvent.map(_.asInstanceOf[A]),
      entry.deltaAckEvent
    )

  override def pushDelta[A : JsonValueCodec](gid: AggregateGid, delta: TaggedDelta[A]) =
    eventRouter(gid).deltaAckEvent.fire(delta.tag)
