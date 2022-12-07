package core.scala.framework.CmRDT

case class EventSource[R](
  val replicaId: String,
  val proofs: List[RoleProof[R]]
):
  def verifyProofs(roles: List[Role[R]]): Boolean =
    roles.forall(role => proofs.exists(
      proof => proof.id == role.id && proof.verify(role, replicaId)
    ))

case class WithEventSource[A, R](
  val inner: A,
  val source: EventSource[R]
)

trait GenericEvent[A, R]:
  def asEffect(source: EventSource[R]): Effect[A, R]

case class CreateEvent[A, R](
  val roles: List[Role[R]],
) extends GenericEvent[A, R]:
  def asEffect(source: EventSource[R]): Effect[A, R] =
    Effect(
      (state: WithContext[A, R]) => state.isContextEmpty,
      (state: WithContext[A, R]) => WithContext(state.inner, roles)
    )
