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
    Fold(initial)(
      specification.mutate.act(current.mutate(_)),

      specification.delta.act(current.applyDelta(_)),
      specification.deltaAck.act(tag => current.acknowledge(tag)),
      
      specification.deflateDeltas.act(_ => current.deflateDeltas),
    )
