package functions

import functions.device.*
import functions.device.services.*
import functions.services.*
// import functions.state.*
// import functions.state.services.*
import scala.scalajs.js

// Most services should be lazy because some services require bindings to azure
// resources but not all function entries may provide all bindings. Non lazy should
// therefor be used with care
trait Services:
  lazy val config: ApplicationConfig
  lazy val connectionContext: ConnectionContextProviderInterface
  lazy val logger: LoggerServiceInterface
  lazy val socketMessaging: SocketMessagingServiceInterface
  lazy val eventStorage: EventStorageService

class ServicesDefault(
  context: js.Dynamic
) extends Services, DeviceServices:
//  lazy val storage = new StorageService(this, context)

  // Core
  lazy val config = new ApplicationConfig(this, context)
  lazy val connectionContext = new ConnectionContextProvider(this, context)
  lazy val logger = new LoggerService(this, context)
  lazy val socketMessaging = new SocketMessagingService(this, context)

  lazy val eventStorage = new EventStorageService(this, context)
