package webapp.services

import scala.concurrent.ExecutionContext.Implicits.global
import sttp.client3.*
import sttp.capabilities.{Effect, WebSockets}
import sttp.model.ResponseMetadata
import sttp.ws.WebSocket
import rescala.default.*

class BackendApiService:
    def hello(username: String) = 
        val response = basicRequest.get(uri"http://localhost:7071/api/hello?name=$username").send(backend)
        Signals.fromFuture(response).map(_.body.toOption)

    private val backend = FetchBackend()
