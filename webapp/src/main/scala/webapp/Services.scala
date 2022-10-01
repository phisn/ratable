package webapp

import webapp.services.*

trait Services:
  lazy val config: ApplicationConfig
  lazy val stateDistribution: StateDistributionService
  lazy val stateProvider: StateProviderService
  lazy val backendApi: BackendApiService
