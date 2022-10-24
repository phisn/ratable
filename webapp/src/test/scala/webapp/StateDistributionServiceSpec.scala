package webapp

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.store.framework.{*, given}
import kofre.base.*

import org.scalatest.*
import org.scalatest.flatspec.*

import org.scalamock.scalatest.*
import org.scalamock.scalatest.MockFactory

import rescala.default.*
import webapp.services.*
import webapp.store.framework.{*, given}

// http://scalamock.org/
// https://www.scalatest.org/user_guide/testing_with_mock_objects

case class TestAggregate(
  id: LWW[Int]
) derives Bottom, DecomposeLattice:
  def equals(other: TestAggregate): Boolean =
    id match {
      case None => other.id == None
      case Some(id) => other.id match {
        case None => false
        case Some(otherId) => id.value == otherId.value 
                           && id.replicaID == otherId.replicaID
      }
    }

given JsonValueCodec[TestAggregate] = JsonCodecMaker.make

class StateDistributionServiceSepc extends AnyFlatSpec:
  val initialAggregate = TestAggregate(LWW.apply(123, "replicaID"))
  val aggregateID = "aggregateID"

  object _statePersistence extends StatePersistanceServiceInterface:
    
    override def storeAggregateSignal[A : JsonValueCodec : Bottom](id: String, factory: A => Signal[A]): Signal[A] =
      assert(id == aggregateID)
      factory(initialAggregate.asInstanceOf[A])

  val _config = ApplicationConfig()

  val _backendApi = BackendApiService(new {
    val config = _config
  })

  val stateDistribution = StateDistributionService(new {
    val backendApi = _backendApi
    val config = _config
    val statePersistence = _statePersistence
  })

  "StateDistributionService" should "load initial aggregaate from StatePersistenceService" in {
    val facade = stateDistribution.registerAggregate[TestAggregate](aggregateID)
    assert(facade.changes.now equals initialAggregate)
  }

  it should "show deltas as changes" in {
    val facade = stateDistribution.registerAggregate[TestAggregate](aggregateID)
    val delta = TestAggregate(LWW.apply(456, "otherReplicaID"))
    
    facade.actions.fire(_ => delta)
    assert(facade.changes.now equals delta)
  }


