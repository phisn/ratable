package functions.services

import core.messages.server.*
import scala.reflect.Selectable.*
import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.typedarray.*
import scalapb.*

trait WebPubSubServiceInterface:
  def sendToAll[A <: ServerMessage.Message](message: A, exclude: Seq[String] = Seq()): Unit
  def sendToAllJson(message: String, exclude: Seq[String] = Seq()): Unit
  def reply[A <: ServerMessage.Message](message: A): Unit

class WebPubSubService(
  services: {
    val connectionContext: ConnectionContextProviderInterface
  }, 
  context: js.Dynamic
) extends WebPubSubServiceInterface:
  def sendToAll[A <: ServerMessage.Message](message: A, exclude: Seq[String] = Seq()) =
    js.Dynamic.literal(
      "actionName" -> "sendToAll",
      "data" -> messageToBuffer(message),
      "dataType" -> "binary",
      "excluded" -> exclude.toJSArray
    )

  def sendToAllJson(message: String, exclude: Seq[String] = Seq()) =
    js.Dynamic.literal(
      "actionName" -> "sendToAll",
      "data" -> message,
      "dataType" -> "json",
      "excluded" -> exclude.toJSArray
    )
  
  def reply[A <: ServerMessage.Message](message: A) =
    js.Dynamic.literal(
      "actionName" -> "sendToConnection",
      "data" -> messageToBuffer(message),
      "dataType" -> "binary",
      "connectionId" -> services.connectionContext.connectionId
    )

  def messageToBuffer[A <: ServerMessage.Message](message: A) =
    ServerMessage(message).toByteArray.toTypedArray.buffer
