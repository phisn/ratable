package webapp.state.services

import core.state.*
import core.state.aggregates.ratable.{*, given}
import scala.reflect.Selectable.*
import webapp.state.*
import webapp.state.framework.*

class ApplicationStateFactory(services: {
  val facadeFactory: FacadeRepositoryFactory
  val statePersistence: StatePersistenceService
}):
  def buildApplicationState: ApplicationState =
    services.statePersistence
      .migrationForRepository(AggregateId.Ratable.toString())
      .boot

    val state = ApplicationState(
      ratables = services.facadeFactory.registerAggregateAsRepository[Ratable](AggregateId.Ratable.toString()),
    )

    state
