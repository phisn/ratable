package webapp.store.framework

import kofre.decompose.containers.DeltaBufferRDT
import rescala.default.*

// Facade exposes the manipulation or reading of the aggregate A
case class Facade[A](
  actions: Evt[DeltaBufferRDT[A] => DeltaBufferRDT[A]],
  changes: Signal[A]
)
