package webapp.components

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.store.aggregates.ratings.given
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.Services
import webapp.given
import webapp.usecases.ratings.*

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import loci.communicator.webrtc
import loci.communicator.webrtc.WebRTC
import loci.communicator.webrtc.WebRTC.ConnectorFactory
import loci.registry.Registry
import org.scalajs.dom.UIEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

def connectionInput(using services: Services) =
  val inputStr = Var("")
  val outputStr = Var("")

  val connectTo = Evt[Unit]()
  val connectFrom = Evt[Unit]()
  val accept = Evt[Unit]()
  
  val codec: JsonValueCodec[webrtc.WebRTC.CompleteSession] = JsonCodecMaker.make

  case class PendingConnection(connector: WebRTC.Connector, session: Future[WebRTC.CompleteSession])

  def webrtcIntermediate(cf: ConnectorFactory) = {
    val p      = Promise[WebRTC.CompleteSession]()
    val answer = cf complete p.success
    PendingConnection(answer, p.future)
  }
  
  var pendingServer: Option[PendingConnection] = None

  connectTo.observe(_ =>
    val res = webrtcIntermediate(WebRTC.offer(services.config.rtcConfig))
    res.session.foreach(s => outputStr.set(writeToString(s)(codec)))
    pendingServer = Some(res)
    services.stateDistribution.registry.connect(res.connector).foreach(_ => outputStr.set(""))
  )

  connectFrom.observe(_ =>
    val res = webrtcIntermediate(WebRTC.answer(services.config.rtcConfig))
    res.session.foreach(s => outputStr.set(writeToString(s)(codec)))
    services.stateDistribution.registry.connect(res.connector).foreach(_ => outputStr.set(""))
    res.connector.set(readFromString(inputStr.now)(codec))
  )

  accept.observe(_ =>
    pendingServer match {
      case Some(ss) => ss.connector.set(readFromString(inputStr.now)(codec))
      case None => 
    }
  )
  
  div(
    div(
      input(
        placeholder := "input",
        marginLeft := "5px",
        onInput.value --> inputStr
      ),
      input(
        placeholder := "output",
        marginLeft := "5px",
        readOnly := true,
        value <-- outputStr
      )
    ),
    div(
      display.flex,
      button("Connect To", onClick.as(()) --> connectTo),
      button(marginLeft := "5px", "Connect From", onClick.as(()) --> connectFrom),
      button(marginLeft := "5px", "Accept", onClick.as(()) --> accept),
    )
  )
