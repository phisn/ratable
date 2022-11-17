package functions.state.processors

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import concurrent.ExecutionContext.Implicits.global
import core.messages.common.*
import core.messages.socket.*
import core.state.aggregates.ratable.*
import core.state.framework.*
import functions.*
import functions.state.*
import scala.util.*

import kofre.base.*

def processRatable(id: String, delta: Ratable)(using services: Services with StateServices) =
  services.logger.trace(s"RatableDeltaMessage ${id}: ${delta._title.read.getOrElse("<empty>")}")
  services.stateProvider.ratables.set(id, delta)

  services.socketMessaging.sendToAll(ServerSocketMessage.Message.Delta(
    DeltaMessage(AggregateGid(id, AggregateType.Ratable), writeToString(delta))
  ))

  services.stateProvider.ratables.get(id).map(_.getOrElse(Bottom[Ratable].empty)).flatMap(ratable =>
    services.stateProvider.ratables.set(
      id,
      Lattice[Ratable].merge(ratable, delta)
    )
  )
