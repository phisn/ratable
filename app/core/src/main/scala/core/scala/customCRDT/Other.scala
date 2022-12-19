/*package core.scala.customCRDT.other

// A role is something that can be required by a event. To be able to create an event, you must prove that you have the required roles.
case class Role[ID](
  val publicKey: String,
  val id: ID,
)

object Role:
  def create[ID](id: ID): (Role[ID], RoleProver[ID]) =
    ???

// A role proof is a proof that you have a role. It is only a proof for your replicaId.
case class RoleProof[ID](
  val proof: String,
  val id: ID,
):
  // Verifies that proof is the replicaID encrypted with the private key of the role.
  def verify(role: Role[ID], replicaId: String): Boolean =
    ???

// A role prover allows you to prove that any replicaId has a role.
case class RoleProver[ID](
  val privateKey: String,
  val id: ID
):
  // Encrypts the replicaId with the private key of the role.
  def prove(replicaId: String): String =
    ???

trait Effect[A]:
  def apply(state: A): A

class Event[A](
  val effect: Effect[A]
)

class CRDT[A](
  val state: A
):
  def effect(event: Event[A]) =
    CRDT(event.effect.apply(state))

class PermissionEvent[A](
  val inner: Event[A],
  val replicaId: String,
  val proofs: List[RoleProof[String]]
):
  def effectOn(state: A): A

trait PermissionEffect[A] extends Effect[A]:
  def verify(state: A): Boolean

class PermissionCRDT[A, ID](
  val inner: CRDT[A],
  val roles: List[Role[ID]]
):
  def verify(event: Event[A]) = ???


class Counter
*/