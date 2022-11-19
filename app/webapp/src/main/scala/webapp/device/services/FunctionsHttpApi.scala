package webapp.device.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.messages.*
import core.messages.common.*
import core.messages.http.*
import core.framework.*
import org.scalajs.dom.{MessageEvent, WebSocket}
import rescala.default.*
import scala.collection.mutable.Map
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import scala.scalajs.js.typedarray.*
import scala.util.*
import sttp.client3.*
import sttp.client3.jsoniter.*
import webapp.services.*

trait FunctionsHttpApiInterface:
  def getAggregate(aggregateMessage: GetAggregateMessage): Future[GetAggregateResponseMessage]
  def getWebPubSubConnection(): Future[WebPubSubConnectionMessage]

class FunctionsHttpApi(services: {
  val config: ApplicationConfigInterface
  val logger: LoggerServiceInterface
}) extends FunctionsHttpApiInterface:
  private val backend = FetchBackend()

  def getAggregate(aggregateMessage: GetAggregateMessage) =
    protoRequest(ClientHttpMessage.Message.GetAggregate(aggregateMessage))
      .map {
        case Success(message) => 
          message
          
        case Failure(exception) => 
          services.logger.error(s"Failed to load aggregate from server for ${aggregateMessage.gid} ${exception.getMessage}")
          throw exception
      }

  def getWebPubSubConnection() =
    services.logger.trace("Sending getWebPubSubConnection request")

    basicRequest.get(uri"${services.config.backendUrl}login?userid=${services.config.replicaID}")
      .response(asJson[WebPubSubConnectionMessage])
      .send(backend)
      .map(_.body match
        case Left(error) => 
          services.logger.error(s"Failed to get web pub sub connection: ${error.getMessage}")
          throw error

        case Right(value) => 
          services.logger.trace("Received successfull getWebPubSubConnection response")
          value
      )

  private def protoRequest(message: ClientHttpMessage.Message) =
    basicRequest.post(uri"${services.config.backendUrl}http")
      .contentType("application/x-protobuf")
      .body(ClientHttpMessage(message).toByteArray)
      .response(asByteArrayAlways)
      .send(backend)
      .map(response => GetAggregateResponseMessage.validate(response.body))
