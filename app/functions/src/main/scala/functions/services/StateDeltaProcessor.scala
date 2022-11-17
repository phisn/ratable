package functions.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.messages.common.*
import core.messages.socket.*
import core.state.aggregates.ratable.*
import core.state.framework.*
import functions.*
import functions.state.*
import functions.state.processors.*
import scala.concurrent.*
import scala.scalajs.js
import scala.util.*

class StateDeltaProcessor(
  services: Services with StateServices, 
  context: js.Dynamic
):
  def processDelta(gid: AggregateGid, deltaJson: String): Future[Unit] =
    implicit val services = this.services

    def routeTo[A: JsonValueCodec](processor: (String, A) => Future[Unit]) =
      processor(gid.aggregateId, readFromString[A](deltaJson))

    gid.aggregateType match
      case AggregateType.Ratable => routeTo(processRatable)
      
      case AggregateType.Unrecognized(unrecognizedValue) => 
        throw new RuntimeException(s"Unrecognized aggregate type: ${unrecognizedValue}")
