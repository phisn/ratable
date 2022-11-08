package webapp.state.framework

import kofre.decompose.containers.DeltaBufferRDT
import rescala.default.*
import scala.concurrent.*

trait FacadeRepository[A]:
  def facade(id: String): Facade[A]
