package functions.handlers

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.messages.client.*
import core.messages.delta_message.*
import core.messages.server.*
import core.store.*
import core.store.aggregates.ratable.*
import core.store.framework.*
import functions.*
import scala.util.*

def deltaMessageHandler(message: DeltaMessage)(using services: Services) =
  val tag = processDelta(message.aggregateId, message.deltaJson)

  services.logger.trace(s"DeltaMessage: aggregateId=${message.aggregateId} processed")

  services.webPubSub.reply(ServerMessage.Message.AcknowledgeDeltaMessage(
    AcknowledgeDeltaMessage(message.aggregateId, tag)
  ))

def processDelta(aggregateId: String, deltaJson: String)(using services: Services): Tag =
  // services.logger.trace(s"DeltaMessage ${aggregateId}: ${deltaJson}")

  def routeTo[A: JsonValueCodec](processor: A => Unit): Tag =
    val tagged = readFromString[TaggedDelta[A]](deltaJson)
    processor(tagged.delta)
    tagged.tag

  AggregateId.valueOf(aggregateId) match
    case AggregateId.Ratable => routeTo(processRatableDelta)

def processRatableDelta(delta: RatableRepository)(using services: Services) =
  delta.inner.foreach((id, ratable) =>
    services.logger.log(s"Processing Ratable $id with title: ${ratable._title.read.getOrElse("<empty>")}")
  )

  services.webPubSub.sendToAll(ServerMessage.Message.DeltaMessage(
    DeltaMessage(AggregateId.Ratable.toString(), writeToString(delta))
  ))

def handleAcknowledgment(aggregateId: String, tag: Tag)(using services: Services) =
  services.logger.trace(s"Sending acknowledgment for aggregateId=${aggregateId} tag=${tag}")
