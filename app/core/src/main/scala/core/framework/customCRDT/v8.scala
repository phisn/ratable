package core.framework.customCRDT.v8

trait Event[A, S]:
  def asEffect(source: S): Effect[A]

case class Effect[A](
  val verify:  A => Boolean,
  val advance: A => A
)

case class CmRDT[A](
  val state: A
)



case class EventSource(
  val replicaId: String
)

case class WithPermission[A, R](
  val inner: A,
  val roles: Map[String, List[R]]
):
  def rolesOrEmpty(replicaId: String) =
    roles.getOrElse(replicaId, List.empty)

trait DefaultPCmRDT[A, R]:
  def state: A
  def roles: List[R]

object PCmRDT:
  def create[A, R](replicaId: String)(using default: DefaultPCmRDT[A, R]) =
    CmRDT(
      WithPermission(
        default.state,
        Map(replicaId -> default.roles)
      )
    )

def verifyRoles[A, R](source: EventSource, roles: R*) =
  (state: WithPermission[A, R]) => state.rolesOrEmpty(source.replicaId).contains(roles)

def mutateWithoutContext[A, R](f: A => A) =
  (state: WithPermission[A, R]) => state.copy(inner = f(state.inner))



enum CounterRoles:
  case Adder

case class Counter(
  val value: Int
)

given DefaultPCmRDT[Counter, CounterRoles] with
  def state = Counter(0)
  def roles = CounterRoles.values.toList

type CounterState = WithPermission[Counter, CounterRoles]

trait CounterEvent extends Event[Counter, EventSource]

/*
case class AddEvent(
  val value: Int
) extends CounterEvent:
  def asEffect(source: EventSource): Effect[CounterState] =
    Effect(
      verifyRoles(source, CounterRoles.Adder),
      mutateWithoutContext(state =>
        state.copy(value = state.value + value)
      )
      */

// Same without helpers:
//      (state) => source.verifyProofs(state.findRoles(CounterRoles.Adder)),
//      (state) => CounterState(state.inner.copy(value = state.inner.value + value), state.roles)
//    )

def main =
  val replicaId = "replicaId"

  // ! start
  val counter = PCmRDT.create[Counter, CounterRoles](replicaId)

  val eventSource = EventSource(replicaId)
  
  // ? somehow distribute(createEventWithSource )

  // ! user wants to increment counter

//  val addEvent = AddEvent(1)
//  val addEffect = addEvent.asEffect(eventSource)

  // ... using effect

/*  if !addEffect.verify(counter.state) then
    throw Exception("Event is not valid")
  
  val newCounter = addEffect.advance(counter.state)
*/
  // ... using event
  
  /*
  val addEventWithSource = WithEventSource(
    addEvent,
    eventSource
  )
  */

  // ? somehow distribute(addEventWithSource)
