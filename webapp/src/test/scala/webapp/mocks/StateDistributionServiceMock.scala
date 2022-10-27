package webapp.mocks

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.store.framework.*
import rescala.default.*
import scala.collection.mutable.Map
import webapp.*
import webapp.services.state.*

class StateDistributionServiceMock extends StateDistributionServiceInterface:
  val eventRouter = Map[String, EventRouterEntry]()

  case class EventRouterEntry(
    deltaEvent: Evt[Any],
    deltaAckEvent: Evt[Tag]
  )

  override def aggregateEventsFor[A : JsonValueCodec](id: String) =
    val entry = EventRouterEntry(
      deltaEvent = Evt[Any](),
      deltaAckEvent = Evt[Tag]()
    )

    eventRouter(id) = entry
    
    (
      entry.deltaEvent.map(_.asInstanceOf[A]),
      entry.deltaAckEvent
    )

  override def pushDelta[A : JsonValueCodec](id: String, delta: TaggedDelta[A]) =
    eventRouter(id).deltaAckEvent.fire(delta.tag)
