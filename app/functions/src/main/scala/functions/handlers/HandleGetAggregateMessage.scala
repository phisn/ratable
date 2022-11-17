package functions.handlers.messages

import com.github.plokhotnyuk.jsoniter_scala.core.*
import concurrent.ExecutionContext.Implicits.global
import core.messages.common.*
import core.messages.http.*
import functions.*
import scala.concurrent.*
import core.state.aggregates.ratable.* 

/*
def getAggregateMessageHandler(message: GetAggregateMessage) =
  println(s"GetAggregateMessage: ${message.aggregateId.id}")
*/

def getAggregateMessageHandler(message: GetAggregateMessage)(using services: Services): Future[RespondAggregateMessage] =
  println(s"GetAggregateMessage: gid=${message.gid}")

  services.stateProvider.ratables.get(
    message.gid.aggregateId
  ).map(optionAggregate =>
    if optionAggregate.isEmpty then
      services.logger.warning(s"GetAggregateMessage: aggregate not found: gid=${message.gid}")

    RespondAggregateMessage(
      optionAggregate.map(writeToString(_))
    )
  )
