package webapp.state.services

import cats.data.*
import cats.implicits.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import collection.immutable.*
import core.framework.*
import core.framework.ecmrdt.*
import core.messages.common.*
import core.messages.http.*
import core.messages.socket.*
import kofre.base.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import scala.util.*
import webapp.services.*
import webapp.device.services.*
import webapp.state.framework.*
import com.google.protobuf.ByteString

class StateDistributionService(services: {
  val aggregateViewProvider: AggregateViewProvider
  val config: ApplicationConfigInterface
  val logger: LoggerServiceInterface
  val functionsSocketApi: FunctionsSocketApiInterface
}):
  val eventMessageHandlers = collection.mutable.Map[AggregateType, EventMessage => Unit]()

  services.functionsSocketApi.listen {
    case ServerSocketMessage.Message.Events(message) =>
      message.events.foreach(event =>
        eventMessageHandlers.get(event.gid.aggregateType) match
          case Some(handler) => handler(event)
          case None => services.logger.error(s"DeltaDispatcherService: No handler for aggregate ${event.gid}")
      )
  }

  def listenForEvents[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec](aggregateType: AggregateType, f: (AggregateGid, ECmRDTEventWrapper[A, C, E]) => Unit)(using Crypt) =
    eventMessageHandlers += aggregateType -> eventMessageHandler[A, C, E](f)

  private def eventMessageHandler[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec]
    (eventHandler: (AggregateGid, ECmRDTEventWrapper[A, C, E]) => Unit)(eventMessage: EventMessage)(using crypt: Crypt): Unit =

    val event = readFromString[ECmRDTEventWrapper[A, C, E]](eventMessage.eventJson)
    val sourceReplicaId = event.eventWithContext.context.replicaId
    
    crypt.verify(sourceReplicaId.publicKey.inner, eventMessage.eventJson, eventMessage.signature.bytes).andThen {
      case Success(true)      => eventHandler(eventMessage.gid, event)
      case Success(false)     => services.logger.error(s"StateDistributionService: Invalid signature for event ${eventMessage.eventJson} from replica ${sourceReplicaId} for gid ${eventMessage.gid}")
      case Failure(exception) => services.logger.error(s"StateDistributionService: Failed to verify signature for event ${eventMessage.eventJson} from replica ${sourceReplicaId} for gid ${eventMessage.gid} with exception ${exception}")
    }

  def distribute[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec]
    (gid: AggregateGid, container: EventBufferContainer[A, C, E])(using Crypt): EitherT[Future, RatableError, Unit] =
    for
      events <- container.events.map(eventMessageFromEvent(gid)).sequence

      result <- EitherT.liftF(services.functionsSocketApi.send(
        ClientSocketMessage.Message.Events(EventsMessage(events.toSeq))
      ))
    yield
      ()

  private def eventMessageFromEvent[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec]
    (gid: AggregateGid)(event: ECmRDTEventWrapper[A, C, E])(using crypt: Crypt): EitherT[Future, RatableError, EventMessage] =
    val eventJson = writeToString(event)

    for
      replicaId <- EitherT.liftF(services.config.replicaId)

      _ <- EitherT.cond(replicaId.public == event.eventWithContext.context.replicaId, (),
        RatableError(s"Event ${eventJson} from replica ${event.eventWithContext.context.replicaId} for gid ${gid} is not from this replica ${services.config.replicaId}")
      )

      signature <- EitherT.liftF(crypt.sign(replicaId.privateKey.inner, eventJson))
    yield
      EventMessage(
        gid,
        eventJson,
        ByteString.copyFrom(signature)
      )

  /*
  def registerMessageHandler[A : JsonValueCodec : Bottom : Lattice](aggregateType: AggregateType) =
    messageHandlerMap += aggregateType -> MessageHandlerEntry(
      deltaMessageHandler[A],
      acknowledgeDeltaMessageHandler[A]
    )
  */

  /*
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
  */
  case class MessageHandlerEntry(
    eventMessageHandler: EventMessage => Unit,
    acknowledgeEventMessageHandler: AcknowledgeEventMessage => Unit
  )
