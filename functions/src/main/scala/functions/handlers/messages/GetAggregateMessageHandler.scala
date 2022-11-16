package functions.handlers.messages

import core.messages.*

def getAggregateMessageHandler(message: GetAggregateMessage) =
  println(s"GetAggregateMessage: ${message.aggregateId.id}")
