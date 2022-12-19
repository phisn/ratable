/*
package core.scala.customCRDT.other2
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





class WithContext[A, R](
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

trait Event[A, R]:
  def verify(state: WithContext[A, R], source: EventSource[R]): Boolean
  def effectOn(state: WithContext[A, R]): WithContext[A, R]

class EventWithSource[A, R](
  val inner: Event[A, R],
  val source: EventSource[R]
):
  def verify(state: WithContext[A, R]): Boolean =
    inner.verify(state, source)

class CmRDT[A, R](
  val state: WithContext[A, R]
):
  def effect(event: Event[A, R]) =
    CmRDT(event.effectOn(state))

object CmRDT:
  // Scala 3
  // create from initial state with roles from all enum values from R
  def create[A, R <: reflect.Enum](initial: A)(using reflect: reflect.Reflection): CmRDT[A, R] =
    val roles = reflect.reflectEnum[R].cases.map(c => Role.create(c.name))
    CmRDT(WithContext(initial, roles.map(_._1)))


enum CounterRoles:
  case Adder, Subtractor

class Counter(
  val value: Int
)


case class AddEvent(
  val amount: Int
)

val add = Effect(
  (event, state, source) => source.verifyProofs(state.findRoles(CounterRoles.Adder)),
  (event, state) => Counter(state.value + event.amount)
)

trait Effect[A, R]:
  def verify(state: WithContext[A, R], source: EventSource[R]): Boolean
  def effectOn(state: WithContext[A, R]): WithContext[A, R]

/*
class AddEvent(
  val amount: Int
) extends Event[Counter, CounterRoles]:
  def verify(state: WithContext[Counter, CounterRoles], source: EventSource[CounterRoles]): Boolean =
    source.verifyProofs(state.findRoles(CounterRoles.Adder))

  def effectOn(state: WithContext[Counter, CounterRoles]): WithContext[Counter, CounterRoles] =
    WithContext(
      Counter(state.inner.value + amount), 
      state.roles
    )

class CommonEffect[R, ID](
  val roles: List[Role[ID]],
  val effect: WithContext[R, ID] => WithContext[R, ID]
)
*/

class DecrementEvent(
  val amount: Int
) extends Event[Counter, CounterRoles]:
  def verify(state: WithContext[Counter, CounterRoles], source: EventSource[CounterRoles]): Boolean =
    source.verifyProofs(state.findRoles(CounterRoles.Adder))

  def effectOn(state: WithContext[Counter, CounterRoles]): WithContext[Counter, CounterRoles] =
    WithContext(
      Counter(state.inner.value + amount), 
      state.roles
    )
*/