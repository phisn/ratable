package webapp

import webapp.services.*
import webapp.state.*
import webapp.state.services.*

// The Services trait contains core services used by usecases, components and pages
// Other Services traits like StateServices can by design only be accessed by other 
// Services. The idea is to abstract away complexity
trait Services:
  lazy val backendApi: BackendApiInterface
  lazy val config: ApplicationConfigInterface
  lazy val logger: LoggerServiceInterface
  
  // Service bootstraps in constructor and wont be accessed
  // so it needs to be non lazy to force execution
  val jsBootstrap: JSBootstrapServiceInterface

  // State handling should be running from the start to setup connection to server
  // Instatiation starts from StateProvider
  val state: StateProvider
  
  lazy val routing: RoutingService

object ServicesDefault extends Services, StateServices:
  lazy val backendApi = BackendApi(this)
  lazy val config = ApplicationConfig(this)
  lazy val logger = LoggerService(this)

  val jsBootstrap = JSBootstrapService(this)

  val state = StateProvider(this)

  lazy val routing = RoutingService(this)

  // state
  lazy val facadeFactory = FacadeFactory(this)
  lazy val stateDistribution = StateDistributionService(this)
  lazy val statePersistence = StatePersistenceService(this)
