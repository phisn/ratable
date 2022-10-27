package webapp

import webapp.services.*
import webapp.services.state.*

trait Services:
  lazy val backendApi: BackendApiServiceInterface
  lazy val config: ApplicationConfigInterface
  
  // Service bootstraps in constructor and wont be accessed
  // so it needs to be non lazy to force execution
  val jsBootstrap: JSBootstrapServiceInterface

  lazy val facadeFactory: FacadeFactory
  lazy val stateDistribution: StateDistributionServiceInterface
  lazy val statePersistence: StatePersistanceServiceInterface

  // State handling should be running from the start to setup connection to server
  // Instatiation starts from StateProvider
  val state: StateProvider
  
  lazy val routing: RoutingService
