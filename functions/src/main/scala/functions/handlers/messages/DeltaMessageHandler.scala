package functions.handlers.messages

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.messages.client.*
import core.messages.delta_message.*
import core.messages.server.*
import core.state.*
import core.state.aggregates.ratable.*
import core.state.framework.*
import functions.*
import functions.handlers.*
import scala.util.*

def deltaMessageHandler(message: DeltaMessage)(using services: Services) =
  val tag = deltaHandler(
    AggregateId(AggregateType.valueOf(message.aggregateTypeId), message.aggregateId), 
    message.deltaJson
  )

  services.logger.trace(s"DeltaMessage: aggregateId=${message.aggregateId} processed")

  services.webPubSub.reply(ServerMessage.Message.AcknowledgeDeltaMessage(
    AcknowledgeDeltaMessage(message.aggregateId, tag)
  ))

def handleAcknowledgment(aggregateId: String, tag: Tag)(using services: Services) =
  services.logger.trace(s"Sending acknowledgment for aggregateId=${aggregateId} tag=${tag}")
