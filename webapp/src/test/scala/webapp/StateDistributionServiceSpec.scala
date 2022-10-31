package webapp

import core.state.framework.{*, given}
import org.scalatest.*
import org.scalatest.flatspec.*
import org.scalatest.matchers.should.Matchers.*
import kofre.base.*
import webapp.aggregates.*
import webapp.mocks.*

class StateDistributionServiceSepc extends AnyFlatSpec:
  val aggregateID = "aggregateID"

  val applicationConfigMock = ApplicationConfigMock()
  val initialAggregate = TestAggregate(123, applicationConfigMock.replicaID)

  val statePersistenceService = StatePersistenceServiceMock(aggregateID, Some(initialAggregate))
  val mockServices = ServicesMock(
    _statePersistence = statePersistenceService,
    _config = applicationConfigMock
  )

  "StateDistributionService" should "load initial aggregate from StatePersistenceService" in {
    val facade = mockServices.facadeFactory.registerAggregate[TestAggregate](aggregateID)
    
    facade.changes.now shouldEqual initialAggregate
  }

  it should "show deltas as changes" in {
    val facade = mockServices.facadeFactory.registerAggregate[TestAggregate](aggregateID)
    val delta = TestAggregate(456, "otherReplicaID")
    
    facade.actions.fire(_ => delta)

    facade.changes.now shouldEqual delta
  }

  it should "merge deltas together" in {
    val facade = mockServices.facadeFactory.registerAggregate[TestAggregate](aggregateID)

    val delta1 = TestAggregate(456, "otherReplicaID", Set(1, 2))
    val delta2 = TestAggregate(789, "otherReplicaID", Set(3, 4))
    val merged = Lattice[TestAggregate].merge(delta1, delta2)
    
    facade.actions.fire(_ => delta1)
    facade.actions.fire(_ => delta2)

    facade.changes.now shouldEqual merged
    
    val delta3 = TestAggregate(456, "otherReplicaID")
    facade.actions.fire(_ => delta3)

    facade.changes.now shouldEqual Lattice[TestAggregate].merge(merged, delta3)
  }

  it should "save changes to StatePersistenceService" in {
    statePersistenceService.changes.clear()
    val facade = mockServices.facadeFactory.registerAggregate[TestAggregate](aggregateID)

    val delta1 = initialAggregate
    val delta2 = TestAggregate(456, "otherReplicaID")
    
    facade.actions.fire(_ => delta1)
    facade.actions.fire(_ => delta2)

    statePersistenceService.changes.toList match
      case (a: TestAggregate) :: (b: TestAggregate) :: tail => 
        a shouldEqual delta1
        b shouldEqual delta2
        tail.isEmpty shouldEqual true
      case _ => fail("StatePersistenceService did not receive the correct changes")
  }
