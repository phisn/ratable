package webapp.state.services

import collection.immutable.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.messages.common.*
import kofre.base.*
import kofre.decompose.containers.*
import rescala.default.*
import rescala.operator.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import webapp.services.*
import webapp.state.framework.{*, given}
import webapp.state.{*, given}
import scala.util.Success

import core.framework.*
import core.messages.common.*
import core.messages.http.*
import core.messages.socket.*
import typings.std.global.TextEncoder
import scala.scalajs.js.typedarray.Int8Array
import core.framework.TaggedDelta
import scala.util.Failure
import webapp.device.services.*
import core.domain.aggregates.ratable.*
import webapp.device.storage.*
import typings.std.stdStrings.storage

class DeltaDispatcherService(services: {
  val logger: LoggerServiceInterface
  val functionsSocketApi: FunctionsSocketApiInterface
}):
  val dispatchToClientMap = collection.mutable.Map[AggregateGid, DispatcherEntry]()
  val deltaMessageHandler = collection.mutable.Map[AggregateType, DeltaMessage => Unit]()

  services.functionsSocketApi.listen {
    case ServerSocketMessage.Message.Delta(message) =>
      deltaMessageHandler.get(message.gid.aggregateType) match
        case Some(handler) => handler(message)
        case None => services.logger.error(s"DeltaDispatcherService: No handler for aggregate type ${message.gid}")
    /*
      Future.successful(dispatchToClientMap.get(message.gid))
        .flatMap {
          case Some(entry) => Future.successful(Some(entry))
          case None => 
            ensureAggregateLoadedMap.get(message.gid.aggregateType) match
              case Some(entry) => 
                entry(message.gid.aggregateId)
                  .map(_.map(_ => dispatchToClientMap.get(message.gid)))

              case None => throw new Exception(s"DeltaDispatcherService: No dispatcher for ${message.gid}")
        }
        .andThen {
          case Success(Some(entry)) =>
            entry.delta.fire(message)
        }

      dispatchToClientMap.get(message.gid) match
        case Some(entry) => entry.delta.fire(message)
        case None => services.logger.error(s"Received delta for unknown aggregate type ${message.gid}")
    */

    case ServerSocketMessage.Message.AcknowledgeDelta(message) =>
      dispatchToClientMap.get(message.gid) match
        case Some(entry) => entry.deltaAck.fire(message)
        case None => services.logger.error(s"Received acknowledge delta for unknown aggregate type ${message.gid}")
  }

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
          services.logger.error(s"Failed to dispatch delta $delta because ${exception}")
      }

  def registerAggregateType[A : JsonValueCodec : Bottom : Lattice](
    aggregateType: AggregateType,
    facadeRepository: FacadeRepository[A]
  ) =
    deltaMessageHandler += aggregateType -> (
      (message: DeltaMessage) => facadeRepository.mutate(
        message.gid.aggregateId, 
        _ => readFromString[A](message.deltaJson)
      )
    )

  def listenToServerDispatcher[A : JsonValueCodec : Bottom : Lattice](gid: AggregateGid): (Event[A], Event[Tag]) =
    val entry = dispatchToClientMap.getOrElseUpdate(gid, DispatcherEntry())

    (
      entry.delta.map(message => readFromString(message.deltaJson)),
      entry.deltaAck.map(_.tag)
    )

  case class DispatcherEntry(
    delta: Evt[DeltaMessage] = Evt[DeltaMessage](),
    deltaAck: Evt[AcknowledgeDeltaMessage] = Evt[AcknowledgeDeltaMessage]()
  )
