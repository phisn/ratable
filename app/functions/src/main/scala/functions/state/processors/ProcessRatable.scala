package functions.state.processors

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.messages.common.*
import core.messages.socket.*
import core.state.aggregates.ratable.*
import core.state.framework.*
import functions.*
import functions.state.*
import scala.util.*

def processRatable(id: String, delta: Ratable)(using services: Services with StateServices) =
  services.logger.trace(s"RatableDeltaMessage ${id}: ${delta._title.read.getOrElse("<empty>")}")
  services.stateProvider.ratables.set(id, delta)

  services.socketMessaging.sendToAll(ServerSocketMessage.Message.Delta(
    DeltaMessage(AggregateGid(id, AggregateType.Ratable), writeToString(delta))
  ))
