
/*
package webapp

import core.messages.common.*
import core.framework.{*, given}
import org.scalatest.*
import org.scalatest.flatspec.*
import org.scalatest.matchers.should.Matchers.*
import kofre.base.*
import rescala.default.*
import webapp.aggregates.*
import webapp.mocks.*
import webapp.state.framework.*

class AggregateFactorySpec extends AsyncFlatSpec:
  val aggregateGid = AggregateGid("aggregateId", AggregateType.Ratable)
  
  val aggregate = TestAggregate(123, "abc", Set(1, 2, 3))
  val otherAggregate = TestAggregate(456, "cde", Set(2, 3, 4))

  implicit override def executionContext = scala.concurrent.ExecutionContext.Implicits.global

  "Aggregate" should "should be created by aggregateFactory and contain initial value" in {
    val services = ServicesMock()
    val actions = Evt[TestAggregate => TestAggregate]()

    val aggregateSignal = services.aggregateFactory.createAggregateSignal
      (actions, aggregateGid)
      (DeltaContainer(aggregate))

    aggregateSignal.now.inner shouldEqual aggregate
  }

  it should "show deltas as changes" in {
    val services = ServicesMock()
    val actions = Evt[TestAggregate => TestAggregate]()

    val aggregateSignal = services.aggregateFactory.createAggregateSignal
      (actions, aggregateGid)
      (Bottom.empty)
    
    actions.fire(_ => aggregate)

    aggregateSignal.now.inner shouldEqual aggregate
  }

  it should "merge deltas together" in {
    val services = ServicesMock()
    val actions = Evt[TestAggregate => TestAggregate]()

    val aggregateSignal = services.aggregateFactory.createAggregateSignal
      (actions, aggregateGid)
      (Bottom.empty)
    
    actions.fire(_ => aggregate)
    aggregateSignal.now.inner shouldEqual aggregate
    
    val delta = TestAggregate(LWW.empty, Set(
      LWW(3, services.config.replicaID), 
      LWW(4, services.config.replicaID), 
      LWW(5, services.config.replicaID)
    ))

    actions.fire(_ => delta)
    aggregateSignal.now.inner shouldEqual Lattice[TestAggregate].merge(aggregate, delta)
  }

  /*
  it should "save changes to StatePersistenceService" in {
    val statePersistence = StatePersistenceServiceMock()
    val services = ServicesMock(
      _statePersistence = statePersistence
    )
    val actions = Evt[TestAggregate => TestAggregate]()

    val aggregateSignal = services.aggregateFactory.createAggregateSignal
      (actions, aggregateGid)
      (Bottom.empty)

    actions.fire(_ => aggregate)
    actions.fire(_ => otherAggregate)

    val firstSave = DeltaContainer(Bottom[TestAggregate].empty).mutate(_ => aggregate)
    val secondSave = firstSave.mutate(_ => otherAggregate)

    statePersistence.saves shouldEqual Seq(
      (aggregateGid, firstSave),
      (aggregateGid, secondSave)
    )
  }
  */
*/