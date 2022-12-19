package core.framework.customCRDT.v9

// replicaId: hashed replica public key
// proof: 

// bottom layer protocol

trait Event[A, C]:
  def asEffect(context: C): Effect[A]

case class Effect[A](
  val verify:  A => Boolean,
  val advance: A => A
)

case class CmRDT[A](
  val state: A
)

case class PermissionContext[P](
  val replicaId: String,
  val proofs: Set[P]
)

type PermissionEvent[A, P] = Event[A, PermissionContext[P]]

// upper layer protocol



// test

// without permission
object t1:
  enum CounterRoles:
    case Adder

  case class Counter(
    val value: Int
  )

  trait CounterEvent extends PermissionEvent[Counter, CounterRoles]

  case class AddEvent(
    val value: Int
  ) extends CounterEvent:
    def asEffect(context: PermissionContext[CounterRoles]) =
      Effect(
        (state: Counter) => context.proofs.contains(CounterRoles.Adder),
        (state: Counter) => Counter(state.value + value)
      )

// with basic permission by roles
object t2:
  enum CounterRoles:
    case Adder

  case class Counter(
    val value: Int,
    val roles: Map[String, Set[CounterRoles]]
  )

  trait CounterEvent extends PermissionEvent[Counter, CounterRoles]

  case class AddEvent(
    val value: Int
  ) extends CounterEvent:
    def asEffect(context: PermissionContext[CounterRoles]) =
      Effect(
        (state: Counter) => state.roles.getOrElse(context.replicaId, Set.empty).contains(CounterRoles.Adder),
        (state: Counter) => state.copy(value = state.value + value)
      )

// 

case class Context[P](
  val replicaId: String,
  val proofs: Set[P]
)

trait Container[P, D]:
  def verify(delta: D, context: Context[P]): Boolean
  def apply(delta: D, context: Context[P]): Container[P, D]

case class EventCRDTContainer[A, P](
  val state: A
) extends Container[P, Event[A, Context[P]]]:
  def verify(delta: Event[A, Context[P]], context: Context[P]): Boolean =
    delta.asEffect(context).verify(state)

  def apply(delta: Event[A, Context[P]], context: Context[P]): EventCRDTContainer[A, P] =
    EventCRDTContainer(delta.asEffect(context).advance(state))
