package webapp.device.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.messages.*
import core.messages.common.*
import core.messages.socket.*
import core.framework.*
import org.scalajs.dom.{Event, MessageEvent, WebSocket}
import rescala.default.*
import scala.collection.mutable.Map
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import scala.scalajs.js.typedarray.*
import scala.util.*
import scalapb.*
import sttp.client3.*
import sttp.client3.jsoniter.*
import webapp.services.*

trait FunctionsSocketApiInterface:
  def send[A <: ClientSocketMessage.Message](message: A): Future[Unit]
  def listen(listener: ServerSocketMessage => Unit): Unit

class FunctionsSocketApi(services: {
  val config: ApplicationConfigInterface
  val functionsHttpApi: FunctionsHttpApiInterface
  val logger: LoggerServiceInterface
}) extends FunctionsSocketApiInterface:
  def send[A <: ClientSocketMessage.Message](message: A) =
    futureWebsocket.map { ws =>
      val wrapped = ClientSocketMessage(message)

      services.logger.trace(s"Sending message: ${wrapped.toProtoString}")
      ws.send(wrapped.toByteArray.toTypedArray.buffer)
    }

  def listen(listener: ServerSocketMessage => Unit) =
    listeners.add(listener)

  private def onOpen(event: Event) =
    services.logger.log("Websocket connection opened")

  private def onMessage(event: MessageEvent) =
    ServerSocketMessage.validate(
      new Int8Array(event.data.asInstanceOf[ArrayBuffer]).toArray
    ) match
      case Success(value) =>
        services.logger.trace(s"Received socket message: n=${value.message.number}")
        listeners.foreach(_.apply(value))
        
      case Failure(exception) => 
        services.logger.error(s"Could not parse server message: $exception")
        exception.printStackTrace()

  val futureWebsocket = services.functionsHttpApi.getWebPubSubConnection()
    .map { connection =>
      val ws = new WebSocket(connection.url)
      ws.binaryType = "arraybuffer"

      ws.onopen = onOpen(_)
      ws.onmessage = onMessage(_)

      ws
    }
  
  val listeners = collection.mutable.Set[ServerSocketMessage => Unit]()
