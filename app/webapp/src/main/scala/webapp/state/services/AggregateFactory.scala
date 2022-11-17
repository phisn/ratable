package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.messages.common.*
import core.state.*
import kofre.base.*
import org.scalajs.dom
import org.scalajs.dom.*
import reflect.Selectable.reflectiveSelectable
import rescala.default.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.services.*
import webapp.state.framework.*

case class AggregateContext[A](
)

class AggregateFactory(services: {
  val logger: LoggerServiceInterface
  val facadeRepositoryFactory: FacadeRepositoryFactory
  val statePersistence: StatePersistenceServiceInterface
  val stateDistribution: StateDistributionServiceInterface
  val jsUtility: JsUtilityServiceInterface
}):
  def createAggregateSignal[A : JsonValueCodec : Bottom : Lattice](
    actions: Evt[A => A],
    gid: AggregateGid,
  )(
    initial: DeltaContainer[A]
  ) =
    val (
      deltaEvt,
      deltaAckEvt
    ) = services.stateDistribution.aggregateEventsFor[A](gid)

    val offlineEvent = services.jsUtility.windowEventAsEvent("offline")

    val signal = Events.foldAll(initial) { state => 
      Seq(
        // Actions received from the client are applied directly to the state
        actions.act(action => state.mutate(action)),

        offlineEvent.act(_ => state.deflateDeltas),

        // Deltas with changes from other clients received from the server
        deltaEvt.act(delta => state.applyDelta(delta)),

        // Delta acks are sent as a response to merged deltas and contain the tag of the merged delta
        deltaAckEvt.act(tag => state.acknowledge(tag)),
      )
    }

    signal.changed.observe { _ =>
      services.statePersistence.saveAggregate(gid, signal.now)
    }
    
    // When the state changes by an action, send the delta to the server
    actions.observe { _ =>
      services.stateDistribution.pushDelta(gid, signal.now.mergedDeltas)
    }

    signal
