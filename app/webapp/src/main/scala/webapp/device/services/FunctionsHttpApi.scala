package webapp.device.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import concurrent.duration.DurationInt
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
import scalapb.*

trait FunctionsHttpApiInterface:
  def getAggregate(aggregateMessage: GetAggregateEventsMessage): Future[GetAggregateEventsResponse]
  def getWebPubSubConnection(using Crypt): Future[WebPubSubConnectionMessage]

class FunctionsHttpApi(services: {
  val config: ApplicationConfigInterface
  val logger: LoggerServiceInterface
}) extends FunctionsHttpApiInterface:
  private val backend = FetchBackend()

  def getAggregate(aggregateMessage: GetAggregateEventsMessage) =
    services.logger.trace(s"Sending get aggregate message: ${aggregateMessage.gid}")

    protoRequest[GetAggregateEventsResponse](ClientHttpMessage.Message.Get(aggregateMessage))
      .map {
        case Success(message) =>
          services.logger.trace(s"Received get aggregate response: ${message.toProtoString}")
          message
          
        case Failure(exception) => 
          services.logger.error(s"Failed to load aggregate from server for ${aggregateMessage.gid} ${exception.getMessage}")
          throw exception
      }

  def getWebPubSubConnection(using Crypt) =
    services.logger.trace("Sending getWebPubSubConnection request")
    
    for
      replicaId <- services.config.replicaId
      
      result <- basicRequest.get(uri"${services.config.backendUrl}login?userid=${replicaId.publicKey}")
        .response(asJson[WebPubSubConnectionMessage])
        .send(backend)
        .map(_.body match
          case Left(error) => throw error
          case Right(value) => 
            services.logger.trace("Received successfull getWebPubSubConnection response")
            value
        )
    yield
      result

  private def protoRequest[I <: GeneratedMessage](message: ClientHttpMessage.Message)(using companion: GeneratedMessageCompanion[I]) =
    basicRequest.post(uri"${services.config.backendUrl}http")
      .contentType("application/x-protobuf")
      .body(ClientHttpMessage(message).toByteArray)
      .response(asByteArrayAlways)
      .readTimeout(10.seconds)
      .send(backend)
      .map(response => companion.validate(response.body))
