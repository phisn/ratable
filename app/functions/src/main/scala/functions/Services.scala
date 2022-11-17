package functions

import functions.services.*
import functions.state.*
import functions.state.services.*
import scala.scalajs.js

// Most services should be lazy because some services require bindings to azure
// resources but not all function entries may provide all bindings. Non lazy should
// therefor be used with care
trait Services:
  lazy val config: ApplicationConfig
  lazy val connectionContext: ConnectionContextProviderInterface
  lazy val logger: LoggerServiceInterface
  lazy val socketMessaging: SocketMessagingServiceInterface
  lazy val stateDeltaProcessor: StateDeltaProcessor
  lazy val stateProvider: StateProviderService

class ProductionServices(
  context: js.Dynamic
) extends Services, StateServices:
  lazy val config = new ApplicationConfig(this, context)
  lazy val connectionContext = new ConnectionContextProvider(this, context)
  lazy val logger = new LoggerService(this, context)
  lazy val socketMessaging = new SocketMessagingService(this, context)
  lazy val stateDeltaProcessor = new StateDeltaProcessor(this, context)
  lazy val stateProvider = new StateProviderService(this, context)

  lazy val aggregateRepositoryFactory = new AggregateRepositoryFactory(this, context)
