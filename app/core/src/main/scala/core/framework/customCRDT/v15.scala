package core.framework.customCRDT.v15

import core.framework.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

/*
Client sends:
{
  "replicaId": "...",
  "signature": "...",
  "event": {
    "type": "...",
    "extensions": {
      "permission": PermissionContext(claims = Set(...))
    },
    "inner": AddEvent(1)
  }
}
*/

trait Event[A, C]:
  def asEffect: Effect[A, C]

case class Effect[A, C](
  val verify:  (A, C) => Boolean,
  val advance: (A, C) => A
)

trait ContextExtension[C]:
  def verifier: ExtensionVerifier[C]

case class ExtensionVerifier[C](
  val verify: C => Future[Boolean]
)

case class EventWrapper[A, C, E <: Event[A, C]](
  val extensions: Set[ContextExtension[_]],
  val inner: E
)

trait ECmRDTExtension:
  def verify: Future[Boolean]

case class ECmRDT(
  val extensions: Set[ECmRDTExtension]
)
