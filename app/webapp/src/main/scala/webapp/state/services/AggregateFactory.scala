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
  val functionsSocketApi: FunctionsSocketApiInterface
  val stateDistribution: StateDistributionService
  val stateStorage: StateStorageService
  val window: WindowServiceInterface
}):
  def createSignal[A : JsonValueCodec : Bottom : Lattice](gid: AggregateGid, initial: DeltaContainer[A]): AggregateFacade[A] =
    val dispatchFailureEvent = Evt[Unit]()
    
    val mutationEvent = Evt[A => A]()
    val deltaEvent = Evt[A]()
    val deltaAckEvent = Evt[Tag]()

    // Maybe extract deflateDeltas into AggregateFacade provide activation from outside
    val mutationOfflineEvent = mutationEvent
      .filter(_ => !services.window.isOnline)
      .dropParam

    val mutationOnlineEvent = mutationEvent
      .filter(_ => services.window.isOnline)
      .dropParam

    val signal = aggregateSignalFromSpecification(initial, AggregateSpecificationByEvents(
      mutate = mutationEvent,
      delta = deltaEvent,
      deltaAck = deltaAckEvent,
      deflateDeltas = mutationOfflineEvent || dispatchFailureEvent,
    ))

    // Side effects here dont feel very smooth. Maybe we can do better or do them somewhere else
    // Problem is that we must handle side effects for every created signal. There arent many places where
    // we currently could handle them. Maybe we need additional abstraction for that.

    val connectAndUnsendDeltaEvent = services.functionsSocketApi.connected
      .filter(_ => !signal.now.deltas.isEmpty)

    val dispatchEvent = mutationOnlineEvent || connectAndUnsendDeltaEvent

    dispatchEvent.observe(_ =>
      services.logger.trace(s"Dispatching deltas to server for $gid")

      services.stateDistribution.dispatchToServer(gid, signal.now.mergedDeltas)
        .andThen {
          case Failure(exception) =>
            services.logger.error(s"Failed to dispatch deltas to server: id=$gid ${exception.getMessage}")
            dispatchFailureEvent.fire(())
        }
    )

    signal.observe(aggregate =>
      services.stateStorage.save(gid, aggregate)
    )

    AggregateFacade(
      signal,
      mutationEvent,
      deltaEvent,
      deltaAckEvent,
    )

  case class AggregateSpecificationByEvents[A : JsonValueCodec : Bottom : Lattice](
    val mutate: Event[A => A],

    val delta: Event[A],
    val deltaAck: Event[Tag],

    val deflateDeltas: Event[Unit],
  )

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

    /*
    val dispatchFailureEvent = Evt[Unit]()
    val actionWhileOfflineEvent = mutationEvent
      .filter(_ => !services.window.isOnline)
      .dropParam

    val deltaEvent = Evt[A]()
    val deltaAckEvent = deltaAckDispatcher.getOrElseUpdate(gid, Evt[Tag]())

    deltaAckEvent.observe(ack =>
      services.logger.trace(s"Delta ack received for ${gid} ${ack}")
    )

    val signal = aggregateSignalFromSpecification(initial, AggregateSpecificationByEvents(
      mutate = mutationEvent,
      delta = deltaEvent,
      deltaAck = deltaAckEvent,
      deflateDeltas = actionWhileOfflineEvent || dispatchFailureEvent,
    ))

    val connectWithOpenDeltasEvent = services.functionsSocketApi.connected
      .filter(_ => !signal.now.deltas.isEmpty)

    val dispatchDeltas = connectWithOpenDeltasEvent || mutationEvent
     
    dispatchDeltas.observe(_ => 
      services.logger.trace(s"Dispatching deltas to server for $gid")

      services.deltaDispatcher.dispatchToServer(gid, signal.now.mergedDeltas)
        .andThen {
          case Failure(exception) =>
            services.logger.error(s"Failed to dispatch deltas to server: ${exception.getMessage}")
            dispatchFailureEvent.fire(())
        }
    )

    signal.map(_.inner)
    */

  /*
  private val deltaAckDispatcher = collection.mutable.Map[AggregateGid, Evt[Tag]]()

  services.functionsSocketApi.listen {
    case ServerSocketMessage.Message.AcknowledgeDelta(message) =>
      deltaAckDispatcher.get(message.gid) match
        case Some(entry) => 
          entry.fire(message.tag)

        case None => 
          services.logger.error(s"Received acknowledge delta for unknown aggregate type ${message.gid}")
  }
  */
