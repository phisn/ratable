package webapp.state

import webapp.state.services.*

trait StateServices:
  lazy val aggregateFacadeProvider: AggregateFacadeProvider
  lazy val aggregateFactory: AggregateFactory
  lazy val applicationStateFactory: ApplicationStateFactory
  lazy val stateDistribution: StateDistributionService
  lazy val stateStorage: StateStorageService
