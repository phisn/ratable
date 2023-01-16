package function.application.gateway

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import concurrent.ExecutionContext.Implicits.global
import core.messages.common.*
import core.messages.http.*
//import functions.*
//import function.application.handlers.*
import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.js.typedarray.*
import scala.util.*
import scalapb.*

import typings.azureCosmos.mod.*
import typings.std.global.TextEncoder
import typings.std.global.TextDecoder


import org.scalablytyped.runtime.StObject
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSGlobalScope, JSGlobal, JSImport, JSName, JSBracketAccess}

given JsonValueCodec[String] = JsonCodecMaker.make

object HttpEntry:
  @JSExportTopLevel("http")
  def gateway(context: js.Dynamic) =
    ???
/*
    implicit val services = ProductionServices(context)

    services.logger.trace(s"Http called")

    val body = context.req.rawBody.asInstanceOf[String]

    val encoder = TextEncoder()
    val decoder = TextDecoder()

    def respond[A <: GeneratedMessage](message: A) =
      context.res = js.Dynamic.literal(
        "status" -> 200,
        "body" -> js.Dynamic.global.Buffer.from(message.toByteArray.toTypedArray),
        "headers" ->  js.Dynamic.literal(
          "Content-Type" -> "application/x-protobuf"
        )
      )

      context.done()
    
    def dispatch(message: ClientHttpMessage.Message)(implicit services: Services) =
      message match
        /*
        case ClientHttpMessage.Message.GetAggregate(message) => 
          services.logger.trace(s"GetAggregateMessage: aggregateId=${message.gid}")
        
          getAggregateMessageHandler(message).andThen {
            case Success(message) => 
              services.logger.log(message.aggregateJson.getOrElse("null"))
              respond(message)

            case Failure(exception) => 
              services.logger.error(s"Failed to get aggregate: ${exception.getMessage}")
              context.done()
          }
        */

        case ClientHttpMessage.Message.Empty => 
          services.logger.error(s"Http gateway got unkown message")
          context.done()
        case _ => 
          ()

    ClientHttpMessage.validate(new Int8Array(encoder.encode(body).buffer).toArray) match
      case Success(ClientHttpMessage(message, _)) =>
        try
          dispatch(message)
        catch
          case exception: Throwable =>
            services.logger.error(s"Failed to dispatch message n=${message.number}: ${exception.getMessage}")
            context.done()

      case Failure(exception) =>
        services.logger.error(s"Failed to parse message: ${exception.getMessage}")
       context.done()
*/