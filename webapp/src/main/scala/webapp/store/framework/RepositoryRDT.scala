package webapp.store.framework

import kofre.decompose.containers.DeltaBufferRDT
import rescala.default.*

case class RepositoryRDT[Repository](
  actions: Evt[DeltaBufferRDT[Repository] => DeltaBufferRDT[Repository]],
  changes: Signal[Repository]
)
