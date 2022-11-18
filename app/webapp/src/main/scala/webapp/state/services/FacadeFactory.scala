package webapp.state.services

import collection.immutable.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.messages.common.*
import org.scalajs.dom
import org.scalajs.dom.*
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

class FacadeFactory(services: {
  val logger: LoggerServiceInterface
  val statePersistence: StatePersistenceServiceInterface
  val aggregateFactory: AggregateFactory
}):
  // Aggregates that contain a single instance get a IndexedDB table for themselves
  def registerAggregate[A : JsonValueCodec : Bottom : Lattice](aggregateType: AggregateType): Facade[A] =
    val actions = Evt[A => A]()

    val aggregateId = AggregateGid(aggregateType.name, aggregateType)
    
    val aggregateSignalInFuture = services.statePersistence
      // For singleton aggregates we use the aggregate type id as the id
      .loadAggregate(aggregateId)
      
      // .loadAggregate may return None. Default behaviour is to
      // use an empty aggregate. This aggregate is not saved until
      // the first action is fired.
      .map(_.getOrElse(Bottom[DeltaContainer[A]].empty))
      .map(services.aggregateFactory.createAggregateSignal
        (actions, aggregateId)
        (_).map(_.inner))

    actions.recoverEventsUntilCompleted(aggregateSignalInFuture)

    Facade(
      actions,
      Signals.fromFuture(aggregateSignalInFuture).flatten,
    )
