package webapp.mocks

import core.store.aggregates.ratable.*
import core.store.framework.*
import webapp.*
import webapp.mocks.*
import webapp.services.*
import webapp.services.state.*
import webapp.store.{*, given}

case class ServicesMock(
  _backendApi: BackendApiServiceInterface = BackendApiServiceMock(),
  _config: ApplicationConfigInterface = ApplicationConfigMock(),
  _jsBootstrap: JSBootstrapServiceInterface = new JSBootstrapServiceInterface {},
  _statePersistence: StatePersistanceServiceInterface = StatePersistenceServiceMock[RatableRepository](),
) extends Services:
  lazy val backendApi = _backendApi
  lazy val config = _config
  val jsBootstrap = _jsBootstrap

  // Services that are not mocked
  lazy val facadeFactory = FacadeFactory(this)
  lazy val statePersistence = _statePersistence
  lazy val stateDistribution = StateDistributionService()
  lazy val state = StateProvider(this)

  lazy val routing = RoutingService()
