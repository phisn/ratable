package webapp.store

import webapp.store.aggregates.ratings.*
import webapp.store.framework.*

// Boundles access to all aggregates as facades in one central application state
// Used to manipulate or read application state
case class ApplicationState(
  ratings: Facade[Ratings]
)
