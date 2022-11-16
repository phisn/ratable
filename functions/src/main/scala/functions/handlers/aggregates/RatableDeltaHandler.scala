package functions.handlers.aggregates

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.messages.client.*
import core.messages.delta_message.*
import core.messages.server.*
import core.state.*
import core.state.aggregates.ratable.*
import core.state.framework.*
import functions.*
import scala.util.*

def ratableDeltaHandler(id: String, delta: Ratable)(using services: Services) =
  services.logger.trace(s"RatableDeltaMessage ${id}: ${delta._title.read.getOrElse("<empty>")}")

  services.webPubSub.sendToAll(ServerMessage.Message.DeltaMessage(
    DeltaMessage(AggregateType.Ratable, writeToString(delta))
  ))

  /*
  delta.inner.foreach((id, ratable) =>
    services.logger.log(s"Processing Ratable $id with title: ${ratable._title.read.getOrElse("<empty>")}")
  )

  services.webPubSub.sendToAll(ServerMessage.Message.DeltaMessage(
    DeltaMessage(AggregateType.Ratable, writeToString(delta))
  ))
  */
