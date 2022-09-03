package webapp

import cats.effect.SyncIO
import colibri.*
// import loci.registry.{Binding, Registry}
// import loci.transmitter.RemoteRef
import kofre.*
import kofre.datatypes.{GrowOnlyCounter, RGA}
import kofre.decompose.containers.DeltaBufferRDT
import kofre.dotted.Dotted
import kofre.syntax.DottedName
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import rescala.compat.*
import rescala.core.*
import rescala.operator.*

import scalajs.*
import java.util.concurrent.ThreadLocalRandom

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
    (100, "G"),
    (150, "H"),
    (200, "I")
  ).find(_(0) > n).getOrElse((0, "Z"))(1)

@main
def main(): Unit =
  Outwatch.renderInto[SyncIO]("#app", app).unsafeRunSync()

def app =
  val v = Var(0)
  val counter = GrowOnlyCounter.zero

  val replicaId: String = ThreadLocalRandom.current().nextLong().toHexString
  // val registry = new Registry

  val deltaEvt = Evt[DottedName[GrowOnlyCounter]]()
  val incEvt = Evt[Unit]()

  val counterRdt = Events.foldAll(DeltaBufferRDT[GrowOnlyCounter](replicaId, GrowOnlyCounter.zero))( current =>
    Seq(
      incEvt.act(_ => current.resetDeltaBuffer().inc()),
    )
  )

  val rdt: DeltaBufferRDT[GrowOnlyCounter] = DeltaBufferRDT(replicaId, GrowOnlyCounter.zero)
  rdt.resetDeltaBuffer().inc()

  /*
  registry.bindSbj("counter") { (remoteRef: RemoteRef, deltaState: Dotted[GrowOnlyCounter]) =>
    deltaEvt.fire(DottedName(remoteRef.toString, deltaState))
  }

  registry.remoteJoined.monitor((remoteRef: RemoteRef) => null)
  registry.remotes.foreach((remoteRef: RemoteRef) => null)
  registry.remoteLeft.monitor((remoteRef: RemoteRef) => null)
  */

  val s = counterRdt.map(_.value)

  div(
    button("Increment", onClick.as(()) --> incEvt),
    s
  )
