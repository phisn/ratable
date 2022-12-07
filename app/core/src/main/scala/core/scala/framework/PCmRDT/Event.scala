package core.framework.CmRDT

import core.framework.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

case class EventSource[R](
  val replicaId: String,
  val proofs: List[RoleProof[R]]
):
  def hasProofs(roles: List[Role[R]]): Boolean =
    roles.forall(role => proofs.exists(_.id == role.id))

  def verifyProofs(roles: List[Role[R]])(using crypt: Crypt): Future[Boolean] =
    Future.sequence(
        for
          proof <- proofs
          role <- roles.find(_.id == proof.id)
        yield
          proof.verify(role, replicaId)
      )
      .map(_.forall(identity))

case class WithEventSource[A, R](
  val inner: A,
  val source: EventSource[R]
)

trait GenericEvent[A, R]:
  def asEffect(source: EventSource[R]): Effect[A, R]

/*
case class CreateEvent[A, R](
  val roles: List[Role[R]],
) extends GenericEvent[A, R]:
  def asEffect(source: EventSource[R]): Effect[A, R] =
    Effect(
      (state: WithContext[A, R]) => state.isContextEmpty,
      (state: WithContext[A, R]) => WithContext(state.inner, roles)
    )
*/
