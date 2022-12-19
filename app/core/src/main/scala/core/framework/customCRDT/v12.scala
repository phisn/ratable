package core.framework.customCRDT

trait Event[A, C]:
  def asEffect(context: C): Effect[A]

case class Effect[A](
  val verify:  A => Boolean,
  val advance: A => A
)

trait CmRDT[A]:
  def effect[C](event: Event[A, C]): CmRDT[A]

case class PCmRDT[A](
  val state: A
) extends CmRDT[A]

case class PermissionContext[P](
  val replicaId: String,
  val proofs: Set[P]
)

type PermissionEvent[A, P] = Event[A, PermissionContext[P]]

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
      (state: Counter) => state.copy(value = state.value + value)
    )
  