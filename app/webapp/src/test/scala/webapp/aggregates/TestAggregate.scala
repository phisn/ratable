package webapp.aggregates

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.framework.{*, given}
import kofre.base.*

case class TestAggregate(
  id: LWW[Int],
  set: Set[LWW[Int]],
) derives Bottom, DecomposeLattice

object TestAggregate:
  def apply(id: Int, replicaID: String, set: Set[Int] = Set.empty): TestAggregate = 
    TestAggregate(LWW.apply(id, replicaID), set.map(LWW.apply(_, replicaID)))

  def empty = TestAggregate(LWW.empty, Set.empty)

  given JsonValueCodec[TestAggregate] = JsonCodecMaker.make
