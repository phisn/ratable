package function.application.socket

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import concurrent.ExecutionContext.Implicits.global
import core.messages.common.*
import core.messages.socket.*
import core.domain.aggregates.ratable.*
import core.framework.*
import functions.*
import scala.concurrent.*
import scala.util.*

def eventsMessageHandler(message: EventsMessage)(using services: Services): Future[Unit] =
  ???
