package webapp.state

import webapp.state.services.*

trait StateServices:
  lazy val facadeFactory: FacadeFactory
  lazy val stateDistribution: StateDistributionServiceInterface
  lazy val statePersistence: StatePersistanceServiceInterface
