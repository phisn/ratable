/*
package webapp.mocks

import core.domain.aggregates.ratable.*
import core.framework.*
import webapp.*
import webapp.mocks.*
import webapp.services.*
import webapp.state.{*, given}
import webapp.state.services.*

case class ServicesMock(
) extends Services, StateServices
*/

/*
case class ServicesMock(
  _backendApi: BackendApiInterface = BackendApiMock(),
  _config: ApplicationConfigInterface = ApplicationConfigMock(),

  _jsUtilityService: JsUtilityServiceInterface = JsUtilityServiceMock(),

  _stateDistribution: StateDistributionServiceInterface = StateDistributionServiceMock(),
  _statePersistence: StatePersistenceServiceInterface = StatePersistenceServiceMock(),
) extends Services, StateServices:
  lazy val backendApi = _backendApi
  lazy val config = _config
  lazy val logger = LoggerService(this, LogLevel.None)

  val jsUtility = _jsUtilityService

  val state = StateProvider(this)

  lazy val routing = RoutingService(this)

  lazy val applicationStateFactory = ApplicationStateFactory(this)  
  lazy val facadeRepositoryFactory = FacadeRepositoryFactory(this)
  lazy val facadeFactory = FacadeFactory(this)
  lazy val aggregateFactory = AggregateFactory(this)

  lazy val stateDistribution = _stateDistribution
  lazy val statePersistence = _statePersistence
*/