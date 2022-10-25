package webapp.mocks

import core.store.aggregates.ratable.*
import webapp.*
import webapp.mocks.*
import webapp.services.*
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
  lazy val statePersistence = _statePersistence

  // Services that are not mocked
  lazy val stateDistribution = StateDistributionService(this)
  lazy val stateProvider = StateProviderService(this)
  lazy val routing = RoutingService()
