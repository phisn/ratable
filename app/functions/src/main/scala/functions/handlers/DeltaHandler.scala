package functions.handlers

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.messages.common.*
import core.messages.socket.*
import core.state.aggregates.ratable.*
import core.state.framework.*
import functions.*
import functions.handlers.aggregates.*
import scala.util.*
import core.messages.common.AggregateType.Unrecognized

def deltaHandler(aggregateGid: AggregateGid, deltaJson: String)(using services: Services): Tag =
  services.logger.trace(s"DeltaMessage ${aggregateGid}: ${deltaJson}")

  def routeTo[A: JsonValueCodec](processor: (String, A) => Unit): Tag =
    val tagged = readFromString[TaggedDelta[A]](deltaJson)
    processor(aggregateGid.aggregateId, tagged.delta)
    tagged.tag

  aggregateGid.aggregateType match
    case AggregateType.Ratable => routeTo(ratableDeltaHandler)
    case Unrecognized(unrecognizedValue) => 
      throw new RuntimeException(s"Unrecognized aggregate type: ${unrecognizedValue}")
