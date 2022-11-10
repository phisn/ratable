package webapp.state

import webapp.state.services.*

trait StateServices:
  lazy val applicationStateFactory: ApplicationStateFactory

  lazy val facadeFactory: FacadeFactory
  lazy val facadeRepositoryFactory: FacadeRepositoryFactory

  lazy val stateDistribution: StateDistributionServiceInterface
  lazy val statePersistence: StatePersistenceServiceInterface
