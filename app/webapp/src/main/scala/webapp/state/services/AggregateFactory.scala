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

class AggregateFactory(services: {
  val logger: LoggerServiceInterface
  val deltaDispatcher: DeltaDispatcherService
  val functionsSocketApi: FunctionsSocketApiInterface
  val window: WindowServiceInterface
}):
  def createSignal[A : JsonValueCodec : Bottom : Lattice](
    actions: Evt[A => A], 
    gid: AggregateGid, 
    stateStorage: StateStorage
  )(initial: DeltaContainer[A]): Signal[A] =
    val dispatchFailureEvent = Evt[Unit]()
    val actionWhileOfflineEvent = actions
      .filter(_ => !services.window.isOnline)
      .dropParam

    val deltaEvent = Evt[A]()
    val deltaAckEvent = deltaAckDispatcher.getOrElseUpdate(gid, Evt[Tag]())

    deltaAckEvent.observe(ack =>
      services.logger.trace(s"Delta ack received for ${gid} ${ack}")
    )

    val signal = aggregateSignalFromSpecification(initial, AggregateSpecificationByEvents(
      mutate = actions,
      delta = deltaEvent,
      deltaAck = deltaAckEvent,
      deflateDeltas = actionWhileOfflineEvent || dispatchFailureEvent,
    ))

    val connectWithOpenDeltasEvent = services.functionsSocketApi.connected
      .filter(_ => !signal.now.deltas.isEmpty)

    val dispatchDeltas = connectWithOpenDeltasEvent || actions
     
    dispatchDeltas.observe(_ => 
      services.logger.trace(s"Dispatching deltas to server for $gid")

      services.deltaDispatcher.dispatchToServer(gid, signal.now.mergedDeltas)
        .andThen {
          case Failure(exception) =>
            services.logger.error(s"Failed to dispatch deltas to server: ${exception.getMessage}")
            dispatchFailureEvent.fire(())
        }
    )

    signal.changed.observe(_ =>
      stateStorage.save(gid, signal.now)
    )

    signal.map(_.inner)

  def aggregateSignalFromSpecification[A : JsonValueCodec : Bottom : Lattice](
    initial: DeltaContainer[A],
    specification: AggregateSpecificationByEvents[A]
  ) =
    Events.foldAll(initial) { state => 
      Seq(
        specification.mutate.act(state.mutate(_)),

        specification.delta.act(state.applyDelta(_)),
        specification.deltaAck.act(tag => state.acknowledge(tag)),
        
        specification.deflateDeltas.act(_ => state.deflateDeltas),
      )
    }
  
  case class AggregateSpecificationByEvents[A : JsonValueCodec : Bottom : Lattice](
    val mutate: Event[A => A],

    val delta: Event[A],
    val deltaAck: Event[Tag],

    val deflateDeltas: Event[Unit],
  )

  private val deltaAckDispatcher = collection.mutable.Map[AggregateGid, Evt[Tag]]()

  services.functionsSocketApi.listen {
    case ServerSocketMessage.Message.AcknowledgeDelta(message) =>
      deltaAckDispatcher.get(message.gid) match
        case Some(entry) => 
          entry.fire(message.tag)

        case None => 
          services.logger.error(s"Received acknowledge delta for unknown aggregate type ${message.gid}")
  }
