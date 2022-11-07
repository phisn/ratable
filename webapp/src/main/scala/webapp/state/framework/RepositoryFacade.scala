package webapp.state.framework

import kofre.decompose.containers.DeltaBufferRDT
import rescala.default.*
import scala.concurrent.*

// Facade exposes the manipulation or reading of the aggregate A
trait FacadeRepository[A]:
  def facade(id: String): Facade[A]

  def mutate(id: String)(action: A => A) = facade(id).actions.fire(action)
  def get(id: String) = facade(id).changes
