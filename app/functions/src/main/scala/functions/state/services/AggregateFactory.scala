package functions.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import concurrent.ExecutionContext.Implicits.global
import core.messages.common.*
import functions.services.*
import functions.state.*
import functions.state.framework.*
import kofre.base.*
import scala.concurrent.*
import scala.reflect.Selectable.*
import scala.scalajs.js

class AggregateFactory(
  services: {
    val config: ApplicationConfig
    val logger: LoggerServiceInterface
  },
  context: js.Dynamic
)
