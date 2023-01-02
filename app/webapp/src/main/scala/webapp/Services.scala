package webapp

import core.framework.*
import webapp.application.*
import webapp.application.framework.given
import webapp.application.services.*
import webapp.services.*
import webapp.state.*
import webapp.state.services.*
import webapp.device.*
import webapp.device.framework.given
import webapp.device.services.*

// The Services trait contains core services used by usecases, components and pages
// Other Services traits like StateServices can by design only be accessed by other 
// Services. The idea is to abstract away complexity
trait Services:
  lazy val config: ApplicationConfigInterface
  lazy val logger: LoggerServiceInterface

  // State handling should be running from the start to setup connection to server
  // Instatiation starts from StateProvider
  val state: StateProvider

object ServicesDefault extends Services, ApplicationServices, DeviceServices, StateServices:
  // Device
  val applicationInitializer = ApplicationInitializer(this)
  
  lazy val functionsHttpApi = FunctionsHttpApi(this)
  
  // We want socket connection to be established as soon as possible
  val functionsSocketApi = FunctionsSocketApi(this)

  lazy val storage = StorageService(this)

  lazy val window = WindowService(this)

  // Core
  lazy val config = ApplicationConfig(this)
  lazy val logger = LoggerService(this)

  val state = StateProvider(this)

  // Application
  lazy val popup = PopupService(this)
  lazy val local = LocalizationService(this)
  lazy val routing = RoutingService(this)
  
  // State
  lazy val aggregateFacadeProvider = AggregateFacadeProvider(this)
  lazy val aggregateViewProvider = AggregateViewProvider(this)
  lazy val aggregateViewRepositoryFactory = AggregateViewRepositoryFactory(this)
  lazy val applicationStateFactory = ApplicationStateFactory(this)
  
  lazy val stateDistribution = StateDistributionService(this)
  lazy val stateStorage = StateStorageService(this)
