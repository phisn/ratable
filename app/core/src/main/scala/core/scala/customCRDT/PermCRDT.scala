/*
package core.scala.customCRDT

import scala.compiletime.ops.boolean

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

trait Effect[A, ID]:
  def verify(state: A)(using VerificationContext[ID]): Boolean
  def apply(state: A): A

class VerificationContext[ID](

)

case class Event[A, ID](
  val effect: Effect[A, ID],
  val replicaId: String,
  val proofs: List[RoleProof[ID]],
):
  def verify(roles: List[Role[ID]]): Boolean =
    rolesRequired(roles).forall(containsProof)

  private def containsProof(role: Role[ID]) =
    proofs.exists(proof => proof.id == role.id && proof.verify(role, replicaId))

  private def rolesRequired(roles: List[Role[ID]]) =
    effect.rolesRequired.map(id => roles.find(_.id == id)).flatten

case class PermCRDT[A, ID](
  val state: A,
  val roles: List[Role[ID]]
):
  def verify(event: Event[A, ID]) =
    event.verify(roles)

  def mutate(mutation: Mutation[A]): PermCRDT[A] =
    copy(
      state = mutation.mutation.mutate(state)
    )

case object CounterRoles:
  def increment = "increment"

case class Counter(
  val value: Int
)

case class CounterIncreamentMutator(
  val by: Int
) extends Mutator[Counter]:
  def rolesNamesRequired = Set(CounterRoles.increment)

  def mutate(state: Counter) =
    state.copy(value = state.value + by)

def clientCode =
  val (incrementRole, incrementRoleProver) = Role.create(CounterRoles.increment)

  val counter = PermCRDT(Counter(0), List(role))

  val incrementMutation = Mutation(
    CounterIncreamentMutator(3),
    "replicaId",
    List(incrementRoleProver.prove("replicaId"))
  )

  if !counter.verify(incrementMutation) then
    throw Exception("Mutation not verified")
    
  counter.mutate(incrementMutation)
*/

// Application flow
/*

The first client creates the rdt with an token. This includes the roles and provers. The token hashed is the id. The token is not known to the server. 
This creation is an event that will be send to the server. All events are end to end encrypted with the token so the server cannot see the content of the event. 
The server only stores a list of events. Each client with access to the token can now request the events from the server. This event will only contain the roles
and not the provers, so the client cannot create new events requiring proofs. The client now can somehow get access to the provers.

*/
