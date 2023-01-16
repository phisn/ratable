/*
package functions.application.gateways

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import concurrent.ExecutionContext.Implicits.global
import core.domain.aggregates.analytic.*
import core.framework.*
import functions.*
import function.application.handlers.*
import kofre.base.*
import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.js.typedarray.*
import scala.util.*
import scalapb.*

/*
{
    "invocationId": "40f6b234-8c03-49b8-b3ef-",
    "traceContext": {
        "traceparent": "",
        "tracestate": "",
        "attributes": {}
    },
    "executionContext": {
        "invocationId": "40f6b234-8c03-49b8-b3ef-",
        "functionName": "login",
        "functionDirectory": "C:\\Users\\Phisn\\Repos\\local-rating\\app\\functions\\app\\login",
        "retryContext": null
    },
    "bindings": {
        "req": {
            "method": "GET",
            "url": "http://localhost:7071/api/login?userid=f7e67b59f5a03377",
            "originalUrl": "http://localhost:7071/api/login?userid=f7e67b59f5a03377",
            "headers": {
                "accept": "*//*",
                "connection": "keep-alive",
                "host": "localhost:7071",
                "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36",
                "accept-encoding": "gzip, deflate, br",
                "accept-language": "en-US,en;q=0.9",
                "origin": "http://localhost:12345",
                "referer": "http://localhost:12345/",
                "sec-ch-ua": "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"",
                "sec-ch-ua-mobile": "?0",
                "sec-ch-ua-platform": "\"Windows\"",
                "sec-fetch-site": "same-site",
                "sec-fetch-mode": "cors",
                "sec-fetch-dest": "empty"
            },
            "query": {
                "userid": "f7e67b59f5a03377"
            },
            "params": {}
        },
        "connection": {
        }
    },
    "bindingData": {
        "invocationId": "40f6b234-8c03-49b8-b3ef-d30979f00c85",
        "userid": "f7e67b59f5a03377",
        "query": {
            "userid": "f7e67b59f5a03377"
        },
        "headers": {
            "accept": "*//*",
            "connection": "keep-alive",
            "host": "localhost:7071",
            "user-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36",
            "accept-Encoding": "gzip, deflate, br",
            "accept-Language": "en-US,en;q=0.9",
            "origin": "http://localhost:12345",
            "referer": "http://localhost:12345/",
            "sec-ch-ua": "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"",
            "sec-ch-ua-mobile": "?0",
            "sec-ch-ua-platform": "\"Windows\"",
            "sec-Fetch-Site": "same-site",
            "sec-Fetch-Mode": "cors",
            "sec-Fetch-Dest": "empty"
        },
        "sys": {
            "methodName": "login",
            "utcNow": "2022-11-22T14:40:42.693Z",
            "randGuid": "22d6012f-eafa-44fc-afc5-"
        }
    },
    "bindingDefinitions": [
        {
            "name": "req",
            "type": "httpTrigger",
            "direction": "in"
        },
        {
            "name": "res",
            "type": "http",
            "direction": "out"
        },
        {
            "name": "connection",
            "type": "webPubSubConnection",
            "direction": "in"
        }
    ],
    "req": {
        "method": "GET",
        "url": "http://localhost:7071/api/login?userid=f7e67b59f5a03377",
        "originalUrl": "http://localhost:7071/api/login?userid=f7e67b59f5a03377",
        "headers": {
            "accept": "*//*",
            "connection": "keep-alive",
            "host": "localhost:7071",
            "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36",
            "accept-encoding": "gzip, deflate, br",
            "accept-language": "en-US,en;q=0.9",
            "origin": "http://localhost:12345",
            "referer": "http://localhost:12345/",
            "sec-ch-ua": "\"Google Chrome\";v=\"107\", \"Chromium\";v=\"107\", \"Not=A?Brand\";v=\"24\"",
            "sec-ch-ua-mobile": "?0",
            "sec-ch-ua-platform": "\"Windows\"",
            "sec-fetch-site": "same-site",
            "sec-fetch-mode": "cors",
            "sec-fetch-dest": "empty"
        },
        "query": {
            "userid": "f7e67b59f5a03377"
        },
        "params": {}
    },
    "res": {
        "headers": {},
        "cookies": []
    }
}
*/

case class AnalyticsModel(
  val replicaId: String

)

object AnalyticsEntry:
  @JSExportTopLevel("analytics")
  def gateway(context: js.Dynamic) =
    implicit val services = ProductionServices(context)

    services.logger.trace(s"Analytics called")

    val userId = context.req.query.userid.asInstanceOf[String]
    val containerName = "analytics"

    services.storage.container(containerName).get(userId)
      .map(_.getOrElse(Bottom[Analytic].empty))
      .map(Lattice[Analytic].merge(_, Analytic(
        Visit(
          userAgent = context.req.headers.selectDynamic("user-agent").asInstanceOfOr[String]("<undefined>"),
          language  = context.req.headers.selectDynamic("accept-language").asInstanceOfOr[String]("<undefined>"),
          referrer  =  context.req.headers.selectDynamic("referrer").asInstanceOfOr[String]("<undefined>"),

          timestamp = context.bindingData.sys.utcNow.asInstanceOfOr[String]("<undefined>"),
        )
      )))
      .flatMap(services.storage.container(containerName).put(containerName, userId))
      .andThen {
        case Success(_) => services.logger.trace(s"Analytics success")
        case Failure(e) => services.logger.error(s"Analytics failure: $e")
      }
      .andThen {
        case _ => context.done()
      }
*/