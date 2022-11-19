package webapp.device

import webapp.device.services.*

// For testing purposes only device services have to be mocked be independent of the device
trait DeviceServices:
  // Service bootstraps in constructor and wont be accessed
  // so it needs to be non lazy to force execution
  val applicationInitializer: ApplicationInitializerInterface

  lazy val functionsHttpApi: FunctionsHttpApiInterface
  val functionsSocketApi: FunctionsSocketApiInterface

  lazy val storage: StorageServiceInterface

  lazy val window: WindowServiceInterface
