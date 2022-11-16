package functions.entries

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.messages.common.*
import core.messages.socket.*
import functions.*
import functions.handlers.messages.*
import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.js.typedarray.*
import scala.util.*
import scalapb.*

given JsonValueCodec[String] = JsonCodecMaker.make

object HttpEntry {
  @JSExportTopLevel("http")
  def httpGateway(context: js.Dynamic) =
    implicit val services = ProductionServices(context)
    http(services.http.query("type"))

  def http(messageType: String)(using services: Services) =
    ()
}

