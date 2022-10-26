package webapp.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import rescala.default.*
import scala.reflect.Selectable.*
import webapp.services.*
import webapp.services.state.*
import webapp.store.{*, given}
import webapp.store.framework.{*, given}

// Creates facades for aggregates and registers them for distribution
class FacadeFactory(services: {
  val stateDistribution: StateDistributionService
  val statePersistence: StatePersistanceServiceInterface
}):
  def registerAggregate[A : JsonValueCodec : Bottom : Lattice](
    id: String
  ): Facade[A] =
    val actionsEvt = Evt[A => A]()
    val deltaEvt = services.stateDistribution.deltaEventFor[A](id)

    val changes = services.statePersistence.storeAggregateSignal[DeltaContainer[A]](id, init =>
      Events.foldAll(init)(state => Seq(
        actionsEvt.act(action => state.mutate(action)),
        deltaEvt.act(delta => state.applyDelta(delta))
      ))
    ).map(_.inner)

    Facade(
      actionsEvt,
      changes,
    )
