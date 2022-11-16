package functions.entries

import core.messages.client.*
import core.messages.delta_message.*
import core.messages.server.*
import functions.*
import functions.handlers.messages.*
import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.js.typedarray.*
import scala.util.*
import scalapb.*

object SocketEntry {
  @JSExportTopLevel("socket")
  def socketGateway(context: js.Dynamic, data: ArrayBuffer) =
    implicit val services = ProductionServices(context)

    services.logger.trace(s"Socket called: ${data.byteLength}")

    ClientMessage.validate(new Int8Array(data).toArray) match
      case Success(value) => socket(value.message)
      case Failure(exception) => 
        services.logger.error(s"Failed to validate message: ${exception.getMessage}")

  def socket(message: ClientMessage.Message)(using services: Services) =
    message match
      case ClientMessage.Message.DeltaMessage(message) =>
        services.logger.trace(s"DeltaMessage: aggregateId=${message.aggregateId}")
        deltaMessageHandler(message)

      case _ =>
        services.logger.error("Unknown message")
}
