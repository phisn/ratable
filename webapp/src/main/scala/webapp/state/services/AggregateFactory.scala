package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import kofre.base.*
import rescala.default.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.state.framework.*

class AggregateFactory(services: {
  val facadeRepositoryFactory: FacadeRepositoryFactory
  val statePersistence: StatePersistenceServiceInterface
}):
  // TODO: Why does this get a DeltaContainer[A] and only returns A. 
  // Maybe responsibility can somehow be shifted to loading from persistence here?
  def createAggregateSignal[A : JsonValueCodec : Bottom : Lattice](actions: Evt[A => A])(initial: DeltaContainer[A]) =
    /*
    val (
      deltaEvt,
      deltaAckEvt
    ) = services.stateDistribution.aggregateEventsFor[A](id)
    */

    Events.foldAll(initial) { state => 
      Seq(
        // Actions received from the client are applied directly to the state
        actions.act(action => state.mutate(action)),

        // Deltas with changes from other clients received from the server
        // deltaEvt.act(delta => state.applyDelta(delta)),

        // Delta acks are sent as a response to merged deltas and contain the tag of the merged delta
        // deltaAckEvt.act(tag => state.acknowledge(tag)),
      )
    }.map(_.inner)
    
    /*  
    // When the state changes by an action, send the delta to the server
    actionsEvt.zip(changes.changed).observe { _ =>
      services.stateDistribution.pushDelta(id, changes.now.mergedDeltas)
    }
    */
