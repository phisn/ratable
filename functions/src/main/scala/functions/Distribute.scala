package functions

import core.messages.client.*
import core.messages.delta_message.*
import core.messages.server.*
import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.typedarray.*
import scala.util.*
import scalapb.*

class WebPubSubMessage(
  val connectionIds: js.Array[String],
  val message: js.typedarray.Int8Array,
) extends js.Object

class WebPubSubMessages(
  val responses: js.Array[WebPubSubMessage]
) extends js.Object

def messageSendToAll[A <: GeneratedMessage](message: A, exclude: Seq[String] = Seq()) =
  js.Dynamic.literal(
    "actionName" -> "sendToAll",
    "data" -> message.toByteArray.toTypedArray.buffer,
    "dataType" -> "binary",
    "excluded" -> exclude.toJSArray
  )

def messageSendToAllJson(message: String, exclude: Seq[String] = Seq()) =
  js.Dynamic.literal(
    "actionName" -> "sendToAll",
    "data" -> message,
    "dataType" -> "json",
    "excluded" -> exclude.toJSArray
  )

object Distribute {
  @JSExportTopLevel("distribute")
  def distribute(context: js.Dynamic, data: ArrayBuffer): js.Promise[Unit] =
    context.log(s"Distribute called: ${data.byteLength}")

    ClientMessage.validate(new Int8Array(data).toArray) match
      case Success(value) => value.message match
        case ClientMessage.Message.DeltaMessage(message) =>
          context.log(s"DeltaMessage: ${message.deltaJson}")
          message.toByteArray

        case _ =>
          context.log("Unknown message")
      case Failure(exception) => 
        context.log(s"Failed to validate message: ${exception.getMessage}")

    js.Promise.resolve(())
}