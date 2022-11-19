package webapp.state.framework

import kofre.decompose.containers.DeltaBufferRDT
import rescala.default.*
import scala.concurrent.*

// Facade exposes the manipulation or reading of the aggregate A
case class Facade[A](
  actions: Evt[A => A],
  changes: Signal[A]
)

trait NewFacade[A]:
  def mutate(f: A => A): Future[Unit]
  def listen: Signal[A]
