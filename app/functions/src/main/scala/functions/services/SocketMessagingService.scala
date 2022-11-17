package functions.services

import core.messages.common.*
import core.messages.socket.*
import scala.reflect.Selectable.*
import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.typedarray.*
import scalapb.*

trait SocketMessagingServiceInterface:
  def sendToAll[A <: ServerSocketMessage.Message](message: A, exclude: Seq[String] = Seq()): Unit
  def sendToAllJson(message: String, exclude: Seq[String] = Seq()): Unit
  def reply[A <: ServerSocketMessage.Message](message: A): Unit

class SocketMessagingService(
  services: {
    val connectionContext: ConnectionContextProviderInterface
    val logger: LoggerServiceInterface
  }, 
  context: js.Dynamic
) extends SocketMessagingServiceInterface:
  context.bindings.actions = js.Array()

  def sendToAll[A <: ServerSocketMessage.Message](message: A, exclude: Seq[String] = Seq()) =
    context.bindings.actions.push(js.Dynamic.literal(
      "actionName" -> "sendToAll",
      "data" -> messageToBuffer(message),
      "dataType" -> "binary",
      "excluded" -> exclude.toJSArray
    ))

  def sendToAllJson(message: String, exclude: Seq[String] = Seq()) =
    context.bindings.actions.push(js.Dynamic.literal(
      "actionName" -> "sendToAll",
      "data" -> message,
      "dataType" -> "json",
      "excluded" -> exclude.toJSArray
    ))
  
  def reply[A <: ServerSocketMessage.Message](message: A) =
    context.bindings.actions.push(js.Dynamic.literal(
      "actionName" -> "sendToConnection",
      "data" -> messageToBuffer(message),
      "dataType" -> "binary",
      "connectionId" -> services.connectionContext.connectionId
    ))

  def messageToBuffer[A <: ServerSocketMessage.Message](message: A): js.Any =
    // Errors and documentation say that this should be ArrayBuffer, but it's not.
    // Only nodejs Buffer class is supported
    js.Dynamic.global.Buffer.from(ServerSocketMessage(message).toByteArray.toTypedArray)
