package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import collection.immutable.*
import core.framework.*
import core.messages.common.*
import core.messages.http.*
import core.messages.socket.*
import kofre.base.*
import rescala.default.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import scala.util.*
import webapp.services.*
import webapp.device.services.*
import webapp.state.framework.*

class StateDistributionService(services: {
  val aggregateFacadeProvider: AggregateFacadeProvider
  val logger: LoggerServiceInterface
  val functionsSocketApi: FunctionsSocketApiInterface
}):
  val messageHandlerMap = collection.mutable.Map[AggregateType, MessageHandlerEntry]()

  services.functionsSocketApi.listen {
    case ServerSocketMessage.Message.Delta(message) =>
      messageHandlerMap.get(message.gid.aggregateType) match
        case Some(handler) => handler.deltaMessageHandler(message)
        case None => services.logger.error(s"DeltaDispatcherService: No handler for aggregate type ${message.gid}")

    case ServerSocketMessage.Message.AcknowledgeDelta(message) =>
      messageHandlerMap.get(message.gid.aggregateType) match
        case Some(handler) => handler.acknowledgeDeltaMessageHandler(message)
        case None => services.logger.error(s"DeltaDispatcherService: No handler for aggregate type ${message.gid}")
  }

  private def aggregateMessageHandler[A : JsonValueCodec : Bottom : Lattice](gid: AggregateGid)(handler: AggregateFacade[A] => Unit) =
    services.aggregateFacadeProvider
      .get[A](gid)
      .andThen {
        case Success(Some(aggregateFacade)) => 
          handler(aggregateFacade)

        case Success(None) =>
          services.logger.error(s"DeltaDispatcherService: No aggregate facade for $gid")

        case Failure(exception) =>
          services.logger.error(s"DeltaDispatcherService: Failed to get aggregate facade for $gid with exception $exception")
      }

  private def deltaMessageHandler[A : JsonValueCodec : Bottom : Lattice](message: DeltaMessage) =
    aggregateMessageHandler[A](message.gid) { facade =>
      facade.deltaEvent.fire(readFromString[A](message.deltaJson))
    }

  private def acknowledgeDeltaMessageHandler[A : JsonValueCodec : Bottom : Lattice](message: AcknowledgeDeltaMessage) =
    aggregateMessageHandler[A](message.gid) { facade =>
      facade.deltaAckEvent.fire(message.tag)
    }
  
  def registerMessageHandler[A : JsonValueCodec : Bottom : Lattice](aggregateType: AggregateType) =
    messageHandlerMap += aggregateType -> MessageHandlerEntry(
      deltaMessageHandler[A],
      acknowledgeDeltaMessageHandler[A]
    )

  def dispatchToServer[A : JsonValueCodec : Bottom : Lattice](
    gid: AggregateGid,
    delta: TaggedDelta[A]
  ) =
    services.functionsSocketApi
      .send(ClientSocketMessage.Message.Delta(
        DeltaMessage(
          gid,
          writeToString(delta.delta),
          delta.tag
        )
      ))
      .andThen {
        case Failure(exception) =>
          services.logger.error(s"Failed to dispatch delta gid=$gid delta=$delta because ${exception}")
      }

  case class MessageHandlerEntry(
    deltaMessageHandler: DeltaMessage => Unit,
    acknowledgeDeltaMessageHandler: AcknowledgeDeltaMessage => Unit
  )
