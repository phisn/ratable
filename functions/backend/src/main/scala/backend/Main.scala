package backend

import core.{given, *}
import com.azure.messaging.webpubsub.*
import com.azure.messaging.webpubsub.models.*
import com.microsoft.azure.functions.*
import com.microsoft.azure.functions.annotation.*
import com.github.plokhotnyuk.jsoniter_scala.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

import java.util.Optional

class Function:
  @FunctionName("Register")
  def run(
    @com.microsoft.azure.functions.annotation.HttpTrigger(
      name = "register",
      methods = Array(HttpMethod.GET))
    request: HttpRequestMessage[Optional[String]],
    context: ExecutionContext
  ): HttpResponseMessage =
    context.getLogger.info("Incoming HTTP request...")

    val pubsub = WebPubSubServiceClientBuilder()
      .connectionString(sys.env("AzureWebPubSubConnectionString"))
      .hub("users")
      .buildClient()

    /*
    val name = Option(request.getQueryParameters.get("name"))

    val message = name match
      case Some(x) => s"Hello, $x"
      case None    => "Please pass a name in the query."
    */

    pubsub.sendToAll(
      writeToString(NewUserMessage(request.getQueryParameters.get("connectionString"))),
      WebPubSubContentType.APPLICATION_JSON
    )

    request
      .createResponseBuilder(HttpStatus.NO_CONTENT)
      .build