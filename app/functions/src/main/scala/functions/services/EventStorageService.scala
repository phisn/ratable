package functions.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.messages.common.*
import core.framework.*
import core.framework.ecmrdt.*
import functions.device.services.*
import scala.reflect.Selectable.*
import scala.scalajs.js

class EventStorageService(
  services: {
  },
  context: js.Dynamic
):
  def store[A : JsonValueCodec, C : JsonValueCodec, E <: Event[A, C] : JsonValueCodec](
    gid: AggregateGid,
    events: List[ECmRDTEventWrapper[A, C, E]]
  ) =
    ???
    /*
    services.storageService
      .container(gid.aggregateType.name)
      .put()
    */

