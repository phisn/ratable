package functions

import functions.services.*
import scala.scalajs.js

trait Services:
  // Most services should be lazy because some services require bindings to azure
  // resources but not all function entries may provide all bindings. Non lazy should
  // therefor be used with care
  lazy val logger: LoggerServiceInterface
  lazy val connectionContext: ConnectionContextProviderInterface
  lazy val webPubSub: WebPubSubServiceInterface

class ProductionServices(
  context: js.Dynamic
) extends Services:
  lazy val logger = new LoggerService(this, context)
  lazy val connectionContext = new ConnectionContextProvider(this, context)
  lazy val webPubSub = new WebPubSubService(this, context)
