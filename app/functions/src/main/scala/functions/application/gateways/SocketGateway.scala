package function.application.gateway

import concurrent.ExecutionContext.Implicits.global
import core.messages.common.*
import core.messages.common.*
import core.messages.socket.*
import functions.*
import function.application.handlers.*
import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.js.typedarray.*
import scala.util.*
import scalapb.*

object SocketEntry:
  @JSExportTopLevel("socket")
  def gateway(context: js.Dynamic, data: ArrayBuffer) =
    implicit val services = ServicesDefault(context)

    services.logger.trace(s"Socket called: ${data.byteLength}")

    def dispatch(message: ClientSocketMessage.Message)(implicit services: Services) =
      message match
        case ClientSocketMessage.Message.Events(message) =>
          ()

        case ClientSocketMessage.Message.Empty => 
          services.logger.error(s"Socket gateway got unkown message")
          context.done()

    ClientSocketMessage.validate(new Int8Array(data).toArray) match
      case Success(ClientSocketMessage(message, _)) =>
        try
          dispatch(message)
        catch
          case exception: Throwable =>
            services.logger.error(s"Failed to dispatch message n=${message.number}: ${exception.getMessage}")
            context.done()

      case Failure(exception) => 
        services.logger.error(s"Failed to validate message: ${exception.getMessage}")
        context.done()
