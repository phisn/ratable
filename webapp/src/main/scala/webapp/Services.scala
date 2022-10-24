package webapp

import webapp.services.*

trait Services:
  lazy val backendApi: BackendApiService
  lazy val config: ApplicationConfig
  
  // Service bootstraps in constructor and wont be accessed
  // so it needs to be non lazy to force execution
  val jsBootstrap: JSBootstrapService

  lazy val stateDistribution: StateDistributionService
  lazy val statePersistence: StatePersistenceService
  lazy val stateProvider: StateProviderService
  
  lazy val routing: RoutingService
