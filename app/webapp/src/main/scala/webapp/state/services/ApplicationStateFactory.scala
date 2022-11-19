package webapp.state.services

import core.messages.common.*
import core.domain.aggregates.ratable.{*, given}
import scala.reflect.Selectable.*
import webapp.state.*
import webapp.state.framework.*

// Abstract all state creation in one place away 
// from the core services into the state module
class ApplicationStateFactory(services: {
  val facadeRepositoryFactory: FacadeRepositoryFactory
}):
  def buildApplicationState: ApplicationState =
    ApplicationState(
      ratables = services.facadeRepositoryFactory.registerAggregateAsRepository[Ratable](AggregateType.Ratable)
    )

import collection.immutable.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.messages.common.*
import kofre.base.*
import kofre.decompose.containers.*
import rescala.default.*
import rescala.operator.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import webapp.services.*
import webapp.state.framework.{*, given}
import webapp.state.{*, given}
import scala.util.Success

import core.messages.common.*
import core.messages.http.*
import typings.std.global.TextEncoder
import scala.scalajs.js.typedarray.Int8Array
import core.framework.TaggedDelta
import scala.util.Failure
import webapp.device.services.*

