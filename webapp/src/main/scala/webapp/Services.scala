package webapp

import webapp.services.*

trait Services:
  // Service bootstraps in constructor and wont be accessed
  // so it needs to be non lazy to force execution
  val jsBootstrap: JSBootstrapService
  
  lazy val config: ApplicationConfig
  lazy val stateDistribution: StateDistributionService
  lazy val stateProvider: StateProviderService
  lazy val backendApi: BackendApiService
