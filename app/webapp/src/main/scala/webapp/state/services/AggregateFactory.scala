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
  val window: WindowServiceInterface
}):
  def createSignal[A : JsonValueCodec : Bottom : Lattice](
    actions: Evt[A => A], 
    gid: AggregateGid, 
    stateStorage: StateStorage
  )(initial: DeltaContainer[A]): Signal[A] =
    val (
      deltaEvent,
      deltaAckEvent
    ) = services.deltaDispatcher.listenToServerDispatcher[A](gid)

    val signal = Events.foldAll(initial) { state => 
      Seq(
        // Actions received from the client are applied directly to the state
        actions.act(state.mutate(_)),

        // If we do not have internet access we can
        // fold all deltas received from the server
        actions
          .filter(_ => !services.window.isOnline)
          .act(_ => state.deflateDeltas),
        
        // Deltas with changes from other clients received from the server
        deltaEvent.act(delta => state.applyDelta(delta)),
        
        // Delta acks are sent as a response to merged deltas and contain the tag of the merged delta
        deltaAckEvent.act(tag => state.acknowledge(tag)),
      )
    }

    actions.observe(_ =>
      services.deltaDispatcher.dispatchToServer(gid, signal.now.mergedDeltas)
    )

    signal.changed.observe(_ =>
      stateStorage.save(gid, signal.now)
    )

    signal.map(_.inner)