package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.messages.common.*
import kofre.base.*
import org.scalajs.dom
import org.scalajs.dom.*
import reflect.Selectable.reflectiveSelectable
import rescala.default.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.services.*
import webapp.state.framework.*

class StateSyncService(services: {
  val stateDistribution: StateDistributionServiceInterface
})
