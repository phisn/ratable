package functions.state.processors

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import concurrent.ExecutionContext.Implicits.global
import core.messages.common.*
import core.messages.socket.*
import core.domain.aggregates.ratable.*
import core.framework.*
import functions.*
import functions.state.*
import scala.util.*

import kofre.base.*

def processRatable(id: String, delta: Ratable)(using services: Services with StateServices) =
  services.logger.trace(s"RatableDeltaMessage ${id}: ${delta._title.read.getOrElse("<empty>")}")

  services.socketMessaging.sendToAll(ServerSocketMessage.Message.Delta(
    DeltaMessage(AggregateGid(id, AggregateType.Ratable), writeToString(delta))
  ))

  services.stateProvider.ratables.applyDelta(id, delta)
