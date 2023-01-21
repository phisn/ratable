package functions.device.services

import scala.reflect.Selectable.*
import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.typedarray.*
import scalapb.*

trait ConnectionContextProviderInterface:
  def userId: String
  def connectionId: String

class ConnectionContextProvider(
  services: {},
  context: js.Dynamic
) extends ConnectionContextProviderInterface:
  def userId = context.bindingData.connectionContext.userId.asInstanceOf[String]
  def connectionId = context.bindingData.connectionContext.connectionId.asInstanceOf[String]
