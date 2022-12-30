package webapp.state

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.domain.aggregates.ratable.ecmrdt.*
import webapp.state.framework.*
import rescala.default.*

// Boundles access to all aggregates as facades in one central application state
// Used to manipulate or read application state
case class ApplicationState(
  ratables: AggregateViewRepository[Ratable, RatableContext, RatableEvent]
)
