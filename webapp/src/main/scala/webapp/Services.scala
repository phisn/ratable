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
  val jsUtility: JsUtilityServiceInterface

  // State handling should be running from the start to setup connection to server
  // Instatiation starts from StateProvider
  val state: StateProvider
  
  lazy val routing: RoutingService

object ServicesDefault extends Services, StateServices:
  // Core
  lazy val backendApi = BackendApi(this)
  lazy val config = ApplicationConfig(this)
  lazy val logger = LoggerService(this)

  val jsUtility = JsUtilityService(this)

  val state = StateProvider(this)

  lazy val routing = RoutingService(this)

  // State
  lazy val applicationStateFactory = ApplicationStateFactory(this)
  lazy val facadeRepositoryFactory = FacadeRepositoryFactory(this)
  lazy val facadeFactory = FacadeFactory(this)
  lazy val aggregateFactory = AggregateFactory(this)

  lazy val stateDistribution = StateDistributionService(this)
  lazy val statePersistence = StatePersistenceService(this)
