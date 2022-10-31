package webapp.services

import scala.concurrent.ExecutionContext.Implicits.global
import sttp.client3.*
import sttp.capabilities.{Effect, WebSockets}
import sttp.model.ResponseMetadata
import sttp.ws.WebSocket
import rescala.default.*

import webapp.state.framework.*

import reflect.Selectable.reflectiveSelectable
import cats.effect.syntax.async
import scala.concurrent.Future

trait BackendApiServiceInterface:
  def hello(username: String): Signal[Option[String]]

class BackendApiService(services: {
  val config: ApplicationConfigInterface
}) extends BackendApiServiceInterface:

  def hello(username: String) =
    Signal(Some("Hello " + username))
  /*
    Signals.fromFuture(
      basicRequest.get(uri"${services.config.backendUrl}hello?name=$username").send(backend)
    ).map(_.body.toOption)

  private val backend = FetchBackend()
  */
