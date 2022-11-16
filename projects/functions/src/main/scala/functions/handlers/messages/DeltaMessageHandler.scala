package functions.handlers.messages

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.messages.common.*
import core.messages.socket.*
import core.state.*
import core.state.aggregates.ratable.*
import core.state.framework.*
import functions.*
import functions.handlers.*
import scala.util.*

def deltaMessageHandler(message: DeltaMessage)(using services: Services) =
  val tag = deltaHandler(
    message.gid,
    message.deltaJson
  )

  services.logger.trace(s"DeltaMessage: aggregateId=${message.gid} processed")

  services.webPubSub.reply(ServerSocketMessage.Message.AcknowledgeDelta(
    AcknowledgeDeltaMessage(message.gid, tag)
  ))

def handleAcknowledgment(aggregateId: String, tag: Tag)(using services: Services) =
  services.logger.trace(s"Sending acknowledgment for aggregateId=${aggregateId} tag=${tag}")
