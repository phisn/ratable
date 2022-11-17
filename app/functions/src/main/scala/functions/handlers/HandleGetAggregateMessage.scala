package functions.handlers.messages

import core.messages.common.*
import core.messages.http.*

/*
def getAggregateMessageHandler(message: GetAggregateMessage) =
  println(s"GetAggregateMessage: ${message.aggregateId.id}")
*/

def getAggregateMessageHandler(message: GetAggregateMessage): RespondAggregateMessage =
  println(s"GetAggregateMessage: gid=${message.gid}")

  RespondAggregateMessage(
    "<hello world>"
  )
