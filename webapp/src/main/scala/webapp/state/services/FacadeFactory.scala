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


import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom
import org.scalajs.dom.*
import scala.concurrent.*
import webapp.state.framework.*
import collection.immutable.*
import rescala.operator.*

class FacadeFactory(services: {
  val logger: LoggerServiceInterface
}):
  def registerAggregateAsRepository[A : JsonValueCodec : Bottom : Lattice](
    aggregateTypeId: String
  ): FacadeRepository[A] =
    newMigration(version = 1) { db =>
      val store = db.createObjectStore(aggregateTypeId)
    }

    def registerAggregate(id: String): Facade[A] =
      val actions = Evt[A => A]()
      
      val aggregateSignalInFuture: Future[Signal[A]] = loadAggregate(id)
        .map(buildAggregateSignal(actions))

      recoverMissingActions(
        aggregateSignalInFuture, 
        actions
      )

      val aggregateSignal = Signals
        .fromFuture(aggregateSignalInFuture)
        .flatten

      Facade(
        actions,
        aggregateSignal
      )

    def buildAggregateSignal(actions: Evt[A => A])(initial: A): Signal[A] =
      Events.foldAll(initial) { state => 
        Seq(
          actions.act(action => action(state)),
        )
      }
    
    // Actions fired while future is not yet completed
    // will be replayed after the future is completed in the correct order
    def recoverMissingActions(future: Future[_], actions: Evt[A => A]) =
      val pending = collection.mutable.Queue[A => A]()
      
      actions
        .filter(_ => !future.isCompleted)
        .observe(pending.enqueue(_))

      future.onComplete(_ => pending
// Dequeue not needed, because future will never be incomplete again
//        .dequeueAll(_ => true)
        .foreach(actions.fire(_))
      )
    
    val facades = collection.mutable.Map[
      String, 
      Facade[A],
    ]()

    new FacadeRepository:
      def facade(id: String): Facade[A] = 
        facades.getOrElseUpdate(id, registerAggregate(id))

// Creates facades for aggregates and registers them for distribution and persistence
class _FacadeFactory(services: {
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
    ) = services.stateDistribution.aggregateEventsFor[A](id)

    val changes = services.statePersistence.storeAggregateSignal[DeltaContainer[A]](id, init =>
      Events.foldAll(init)(state => scala.collection.immutable.Seq(
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
      services.stateDistribution.pushDelta(id, changes.now.mergedDeltas)
    }

    Facade(
      actionsEvt,
      changes.map(_.inner),
    )
