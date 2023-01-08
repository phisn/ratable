package webapp.state

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.domain.aggregates.library.*
import core.domain.aggregates.ratable.*
import core.domain.aggregates.ratable.*
import webapp.state.framework.*
import rescala.default.*
import core.domain.aggregates.ratable.{Ratable, RatableContext, RatableEvent}
import core.domain.aggregates.library.{RatableLibrary, RatableLibraryContext, RatableLibraryEvent}

// Boundles access to all aggregates as facades in one central application state
// Used to manipulate or read application state
case class ApplicationState(
  ratables: AggregateViewRepository[Ratable, RatableContext, RatableEvent],
  library: AggregateViewRepository[RatableLibrary, RatableLibraryContext, RatableLibraryEvent]
)
