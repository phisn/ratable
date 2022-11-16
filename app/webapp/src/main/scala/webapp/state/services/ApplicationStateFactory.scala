package webapp.state.services

import core.messages.common.*
import core.state.*
import core.state.aggregates.ratable.{*, given}
import scala.reflect.Selectable.*
import webapp.state.*
import webapp.state.framework.*

// Abstract all state creation in one place away 
// from the core services into the state module
class ApplicationStateFactory(services: {
  val facadeRepositoryFactory: FacadeRepositoryFactory
  val statePersistence: StatePersistenceServiceInterface
}):
  def buildApplicationState: ApplicationState =
    ApplicationState(
      ratables = services.facadeRepositoryFactory.registerAggregateAsRepository[Ratable](AggregateType.Ratable),
    )
