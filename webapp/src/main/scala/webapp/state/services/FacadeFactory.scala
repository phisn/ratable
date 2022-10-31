package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import kofre.decompose.containers.*
import rescala.default.*
import webapp.services.*
import webapp.state.framework.{*, given}
import webapp.state.{*, given}

import scala.reflect.Selectable.*

// Creates facades for aggregates and registers them for distribution and persistence
class FacadeFactory(stateServices: {
  val stateDistribution: StateDistributionServiceInterface
  val statePersistence: StatePersistanceServiceInterface
}):
  def registerAggregate[A : JsonValueCodec : Bottom : Lattice](
    id: String
  ): Facade[A] =
    // Only way of interaction with the state
    val actionsEvt = Evt[A => A]()

    val (
      deltaEvt,
      deltaAckEvt
    ) = stateServices.stateDistribution.aggregateEventsFor[A](id)

    val changes = stateServices.statePersistence.storeAggregateSignal[DeltaContainer[A]](id, init =>
      Events.foldAll(init)(state => Seq(
        // Actions received from the client are applied directly to the state
        actionsEvt.act(action => state.mutate(action)),

        // Deltas with changes from other clients received from the server
        deltaEvt.act(delta => state.applyDelta(delta)),

        // Delta acks are sent as a response to merged deltas and contain the tag of the merged delta
        deltaAckEvt.act(tag => state.acknowledge(tag)),
      ))
    )

    // When the state changes by an action, send the delta to the server
    actionsEvt.zip(changes.changed).observe { _ =>
      stateServices.stateDistribution.pushDelta(id, changes.now.mergedDeltas)
    }

    Facade(
      actionsEvt,
      changes.map(_.inner),
    )
