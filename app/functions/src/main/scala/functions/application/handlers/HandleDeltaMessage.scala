package function.application.handlers

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import concurrent.ExecutionContext.Implicits.global
import core.messages.common.*
import core.messages.common.*
import core.messages.socket.*
import core.domain.aggregates.ratable.*
import core.framework.*
import functions.*
import function.application.handlers.*
import scala.concurrent.*
import scala.util.*

def deltaMessageHandler(message: DeltaMessage)(using services: Services): Future[Unit] =
  services.logger.trace(s"DeltaMessage: aggregateId=${message.gid}")

  // Funny thing. The tag is not needed inside the processor. It only does need to be acknowledged.
  services.stateDeltaProcessor.processDelta(message.gid, message.deltaJson).andThen(_ =>
    services.socketMessaging.reply(ServerSocketMessage.Message.AcknowledgeDelta(
      AcknowledgeDeltaMessage(message.gid, message.tag)
    ))
    
    services.logger.trace(s"DeltaMessage processed")
  )
