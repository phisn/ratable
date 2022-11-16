package functions.handlers

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.messages.client.*
import core.messages.delta_message.*
import core.messages.server.*
import core.state.*
import core.state.aggregates.ratable.*
import core.state.framework.*
import functions.*
import functions.handlers.aggregates.*
import scala.util.*

def deltaHandler(aggregateId: AggregateId, deltaJson: String)(using services: Services): Tag =
  services.logger.trace(s"DeltaMessage ${aggregateId}: ${deltaJson}")

  def routeTo[A: JsonValueCodec](processor: (String, A) => Unit): Tag =
    val tagged = readFromString[TaggedDelta[A]](deltaJson)
    processor(aggregateId.id, tagged.delta)
    tagged.tag

  aggregateId.aggregateType match
    case AggregateType.Ratable => routeTo(ratableDeltaHandler)
