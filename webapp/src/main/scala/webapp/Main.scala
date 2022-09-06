package webapp

import cats.effect.SyncIO
import colibri.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import loci.communicator.webrtc
import loci.communicator.webrtc.WebRTC
import loci.communicator.webrtc.WebRTC.ConnectorFactory
import loci.registry.*
import loci.serializer.jsoniterScala.*
import loci.transmitter.*
import kofre.*
import kofre.base.*
import kofre.datatypes.*
import kofre.decompose.containers.DeltaBufferRDT
import kofre.dotted.Dotted
import kofre.syntax.DottedName
import kofre.time.Dot
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import rescala.compat.*
import rescala.core.*
import rescala.operator.*

import reflect.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.*
import scala.util.*
import scalajs.*
import sourcecode.Text.generate
import webapp.*
import webapp.services.*

// Outwatch documentation:
// https://outwatch.github.io/docs/readme.html

def token_for_number(n: Int): String =
  Seq(
    (5, "A"),
    (10, "B"),
    (15, "C"),
    (20, "D"),
    (40, "E"),
    (80, "F"),
    (100, "1"),
    (150, "2"),
    (200, "3"),
    (250, "4"),
    (300, "5"),
    (400, "6"),
    (500, "7"),
    (600, "8"),
    (700, "9"),
    (850, "0"),
    (900, "X"),
    (1000, "Y"),
  ).find(_(0) > n).getOrElse((0, "Z"))(1)

object ServicesProduction extends Services:
  lazy val distributionConfig = new DistributionConfig
  lazy val stateActionsService = new StateActionsService
  lazy val stateDistributionService = new StateDistributionService(this)
  lazy val stateProviderService = new StateProviderService

@main
def main(): Unit =
  implicit val services = ServicesProduction
  Outwatch.renderInto[SyncIO]("#app", app).unsafeRunSync()

def app(using services: Services) =
  val deltaEvt = Evt[DottedName[GrowOnlyCounter]]()
  val incEvt = Evt[Unit]()

  val counterRdt = Events.foldAll(DeltaBufferRDT[GrowOnlyCounter](services.distributionService.replicaId, GrowOnlyCounter.zero))( current =>
    Seq(
      incEvt.act(_ => current.resetDeltaBuffer().inc()),
      deltaEvt.act(delta => current.resetDeltaBuffer().applyDelta(delta))
    )
  )


  val binding = Binding[Dotted[GrowOnlyCounter] => Unit]("counter")

  registry.bindSbj(binding) { (remoteRef: RemoteRef, deltaState: Dotted[GrowOnlyCounter]) =>
    deltaEvt.fire(DottedName(remoteRef.toString, deltaState))
  }

  kofre.syntax.DottedName

  val testCounter = Var(0)

  val connOutput = Var("")
  val connInput = Var("")

  val connectFromEvt = Evt[Unit]()
  val connectToEvt = Evt[Unit]()
  val connectAcceptEvt = Evt[Unit]()

  case class PendingConnection(connector: WebRTC.Connector, session: Future[WebRTC.CompleteSession])

  def webrtcIntermediate(cf: ConnectorFactory) = {
    val p = Promise[WebRTC.CompleteSession]()
    val answer = cf complete p.success
    PendingConnection(answer, p.future)
  }

  val codec: JsonValueCodec[webrtc.WebRTC.CompleteSession] = JsonCodecMaker.make
  var pendingServer: Option[PendingConnection] = None

  connectFromEvt.observe { _ =>
    val conn = webrtcIntermediate(WebRTC.offer(rtcConfig))
    conn.session.foreach(session => connOutput.set(writeToString(session)(codec)))
    registry.connect(conn.connector).foreach(_ => testCounter.transform(_ + 10))

    pendingServer = Some(conn)
  }

  connectToEvt.observe { _ =>
    val conn = webrtcIntermediate(WebRTC.answer(rtcConfig))
    conn.session.foreach(session => connOutput.set(writeToString(session)(codec)))
    registry.connect(conn.connector).foreach(_ => testCounter.transform(_ + 1000))

    conn.connector.set(readFromString(connInput.now)(codec))
  }

  connectAcceptEvt.observe { _ =>
    pendingServer match
      case Some(conn) =>
        conn.connector.set(readFromString(connInput.now)(codec))
        pendingServer = None
      case None => ()
  }

  registry.remoteJoined.monitor(registerRemote)
  registry.remotes.foreach(registerRemote)
  registry.remoteLeft.monitor(observers(_).disconnect())

  div(
    div(
      button("Increment", onClick.as(()) --> incEvt),
      " ",
      counterRdt.map(_.value),
      ": ",
      counterRdt.map(counter => token_for_number(counter.value))
    ),
    div(
      testCounter
    ),
    div(
      div(
        input(value <-- connOutput, readOnly := true),
      ),
      div(
        input(onInput.value --> connInput),
      ),
      div(
        button("Connect from", onClick.as(()) --> connectFromEvt),
        " ",
        button("Connect to", onClick.as(()) --> connectToEvt),
        " ",
        button("Connect accept", onClick.as(()) --> connectAcceptEvt),
      )
    )
  )
