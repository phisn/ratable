package core.scala.customCRDT.v5

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
  def prove(replicaId: String): RoleProof[ID] =
    ???

case class WithContext[A, R](
  val inner: A,
  val roles: List[Role[R]]
):
  def findRoles(ids: R*) =
    roles.filter(role => ids.contains(role.id))

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

case class Effect[A, R](
  val verify: (WithContext[A, R]) => Boolean,
  val advance:  (WithContext[A, R]) => WithContext[A, R]
)

def verifyRoles[A, R](source: EventSource[R], roles: R*) =
  (state: WithContext[A, R]) => source.verifyProofs(state.findRoles(roles: _*))

def mutateWithoutContext[A, R](f: A => A) =
  (state: WithContext[A, R]) => state.copy(inner = f(state.inner))

trait GenericEvent

case class CreateEvent[R](
  val roles: List[Role[R]],
) extends GenericEvent

class CmRDT[A, R](
  val state: WithContext[A, R]
)

object CmRDT:
  def create[A, R]: (CmRDT[A, R], List[RoleProver[R]], GenericEvent) =
    ???




    

enum CounterRoles:
  case Adder

case class Counter(
  val value: Int
)

trait CounterEvent extends GenericEvent

case class CreateCounterEvent(
  val inner: CreateEvent[CounterRoles]
) extends CounterEvent

case class AddEvent(
  val value: Int
):
  def asEffect(source: EventSource[CounterRoles]): Effect[Counter, CounterRoles] =
    Effect(
      verifyRoles(source, CounterRoles.Adder),
      mutateWithoutContext(state =>
        state.copy(value = state.value + value)
      )

//      (state) => source.verifyProofs(state.findRoles(CounterRoles.Adder)),
//      (state) => WithContext(state.inner.copy(value = state.inner.value + value), state.roles)
    )

def main =
  val replicaId = "replicaId"

  // ! start
  val (counter, provers, createEvent) = CmRDT.create[Counter, CounterRoles]

  val eventSource = EventSource[CounterRoles](
    replicaId,
    proofs = provers.map(_.prove(replicaId))
  )

  val createEventWithSource = WithEventSource(
    createEvent,
    eventSource
  )
  
  // ? somehow distribute(createEventWithSource )

  // ! user wants to increment counter

  val addEvent = AddEvent(1)
  val addEffect = addEvent.asEffect(eventSource)

  // ... using effect

  if !addEffect.verify(counter.state) then
    throw Exception("Event is not valid")
  
  val newCounter = addEffect.advance(counter.state)

  // ... using event
  
  val addEventWithSource = WithEventSource(
    addEvent,
    eventSource
  )

  // ? somehow distribute(addEventWithSource)