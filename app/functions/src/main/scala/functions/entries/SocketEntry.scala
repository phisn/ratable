package functions.entries

import core.messages.common.*
import core.messages.socket.*
import functions.*
import functions.handlers.messages.*
import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.js.typedarray.*
import scala.util.*
import scalapb.*

object SocketEntry {
  @JSExportTopLevel("socket")
  def gateway(context: js.Dynamic, data: ArrayBuffer) =
    implicit val services = ProductionServices(context)

    services.logger.trace(s"Socket called: ${data.byteLength}")

    ClientSocketMessage.validate(new Int8Array(data).toArray) match
      case Success(ClientSocketMessage(ClientSocketMessage.Message.Delta(message), _)) => 
        services.logger.trace(s"DeltaMessage: aggregateId=${message.gid}")
        deltaMessageHandler(message)

      case Success(ClientSocketMessage(ClientSocketMessage.Message.AssociateReplica(message), _)) => 
        services.logger.trace(s"AssociateReplicaMessage: username=${message.username}")
        services.logger.error(s"AssociateReplicaMessage not implemented")

      case Success(ClientSocketMessage(ClientSocketMessage.Message.Empty, _)) =>
        services.logger.error(s"Unknown message")

      case Failure(exception) => 
        services.logger.error(s"Failed to validate message: ${exception.getMessage}")
}
