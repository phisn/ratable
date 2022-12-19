package core.scala.customCRDT.other3

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



case class WithContext[A, R](
  val inner: A,
  val roles: List[Role[R]]
):
  def findRoles(ids: R*) =
    roles.filter(role => ids.contains(role.id))

class EventSource[R](
  val replicaId: String,
  val proofs: List[RoleProof[R]]
):
  def verifyProofs(roles: List[Role[R]]): Boolean =
    roles.forall(role => proofs.exists(
      proof => proof.id == role.id && proof.verify(role, replicaId)
    ))

case class Effect[A, R](
  val verify: (WithContext[A, R], EventSource[R]) => Boolean,
  val apply:  (WithContext[A, R]) => WithContext[A, R]
)

type EffectFromEvent[E, A, R] = (E) => Effect[A, R]

def commonVerify[E, A, R](roles: R*) =
  (_: E, state: WithContext[A, R], source: EventSource[R]) =>
    source.verifyProofs(state.findRoles(roles: _*))

enum CounterRoles:
  case Adder

case class CounterState(
  val count: Int
)

trait CounterEvent()

case class AddEvent(
  val amount: Int
) extends CounterEvent

def main =
  val (adderRole, adderProver) = Role.create(CounterRoles.Adder)
  val adderProof = adderProver.prove("replicaId")
  val addEvent = AddEvent(1)
