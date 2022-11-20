package webapp.device.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.messages.*
import core.messages.common.*
import core.messages.socket.*
import core.framework.*
import org.scalajs.dom.{CloseEvent, Event, ErrorEvent, MessageEvent, WebSocket}
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
  def listen[U](listener: PartialFunction[ServerSocketMessage.Message, U]): Unit
  def connected: rescala.default.Event[Unit] 

class FunctionsSocketApi(services: {
  val config: ApplicationConfigInterface
  val functionsHttpApi: FunctionsHttpApiInterface
  val logger: LoggerServiceInterface
}) extends FunctionsSocketApiInterface:
  // Websocket is a variable and will be overwritten when the connection is reestablished / failed
  var futureWebsocket = openWebsocket

  val listeners = collection.mutable.Set[ServerSocketMessage => Unit]()
  val connectEvent = Evt[Unit]()

  def send[A <: ClientSocketMessage.Message](message: A) =
    futureWebsocket.map { ws =>
      val wrapped = ClientSocketMessage(message)

      services.logger.trace(s"Sending message")
      ws.send(wrapped.toByteArray.toTypedArray.buffer)
    }

  // Partial function allowing to listen to specific messages
  def listen[U](listener: PartialFunction[ServerSocketMessage.Message, U]) =
    listeners.add(message => listener.applyOrElse(message.message, identity))

  def connected = 
    connectEvent

  private def onOpen(event: Event) =
    services.logger.log("Websocket connection opened")
    connectEvent.fire()

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

  private def openWebsocket: Future[WebSocket] =
    services.functionsHttpApi.getWebPubSubConnection()
      .flatMap { connection =>
        services.logger.trace("Connecting to websocket")

        val promise = Promise[WebSocket]()

        val ws = new WebSocket(connection.url)
        ws.binaryType = "arraybuffer"

        ws.onopen = event =>          
          promise.success(ws)
          onOpen(event)

        ws.onmessage = onMessage(_)

        ws.onerror = event =>
          services.logger.error(s"Websocket connection error: ${event.message}")
          promise.failure(new Exception(event.message))
      
        ws.onclose = event =>
          services.logger.log(s"Websocket connection closed: ${event.reason}")
          futureWebsocket = openWebsocket

        promise.future
      }
      // Not using .recoverWith here because we want everyone to know, that the websocket failed to open
      .andThen {
        case Failure(exception) =>
          services.logger.error(s"Failed open websocket: $exception")

          val promise = Promise[WebSocket]()

          scala.scalajs.js.timers.setTimeout(services.config.websocketReconnectInterval) {
            services.logger.trace("Retrying to open websocket")

            openWebsocket.andThen {
              case Success(value) => promise.success(value)
              case Failure(exception) => promise.failure(exception)
            }
          }

          futureWebsocket = promise.future
      }
