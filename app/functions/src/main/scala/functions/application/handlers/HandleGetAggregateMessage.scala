package function.application.handlers

import com.github.plokhotnyuk.jsoniter_scala.core.*
import concurrent.ExecutionContext.Implicits.global
import core.messages.common.*
import core.messages.http.*
import functions.*
import scala.concurrent.*
import core.domain.aggregates.ratable.* 

/*
def getAggregateMessageHandler(message: GetAggregateMessage) =
  println(s"GetAggregateMessage: ${message.aggregateId.id}")
*/

def getAggregateMessageHandler(message: GetAggregateMessage)(using services: Services): Future[GetAggregateResponseMessage] =
  println(s"GetAggregateMessage: gid=${message.gid}")

  services.stateProvider.ratables.get(
    message.gid.aggregateId
  ).map(optionAggregate =>
    if optionAggregate.isEmpty then
      services.logger.warning(s"GetAggregateMessage: aggregate not found: gid=${message.gid}")

    GetAggregateResponseMessage(
      optionAggregate.map(writeToString(_))
    )
  )
