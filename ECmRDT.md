# Extendable CmRDT
ECmRDT is based on the existing concept of CmRDT. Here we mean Operation based CRDT with CmRDT. The goal is to easily and flexibly be able to extend a normal CmRDT with additional functionallity. The need comes from the problem in how to regulate authentication and authorization with CRDT. Here we solve this problem by implementing extensions for our ECmRDT.

## General concepts
To define a ECmRDT we need a Context, a State, an EffectPipeline and Events. The ECmRDT itself later only consists of the State and a vector clock. Events and Context only exist outside our ECmRDT.

```scala
case class ECmRDT[A, C <: IdentityContext](
  val state: A,
  val clock: VectorClock = VectorClock(Map.empty)
)
```

An Context (in the following code examples `C`) is always associated with one user created Event and is created with it. The Context contains metadata about the Event that can be used in extensions to verify if an Event was send legitemitly like the user replicaId or some authorization Claims or to mutate the State in a specific way. The IdentityContext trait provides a replicaId in our Context.

```scala
trait IdentityContext:
  val replicaId: String
```

The State (in the following code examples `A`) is persistent over time and our core data. It is changed by the combination of a Event and a Context.

An Event is defined by the following. Note that an Event can be converted into an Effect where our logic is implemented.

```scala
trait Event[A, C]:
  def asEffect: Effect[A, C]
```

Some Extensions may also define their custom Context or custom State that has to be included in our data type.

## Extensions and EffectPipeline
Extensions can optionally define an Context trait and optionally an State trait but must define an EffectPipeline. It is important that the EffectPipeline can assume any Context or State traits, even of other extensions.

An EffectPipeline is definied like the following

```scala
trait EffectPipeline[A, C]:
  def apply(effect: Effect[A, C]): Effect[A, C]
```

The EffectPipeline itself is a function that transfroms an existing Effect into a new Effect. An Effect here is a combination of a verify and a advance function.

```scala
case class Effect[A, C](
  val verify:  (A, C) => Future[Option[String]],
  val advance: (A, C) => Future[A]
)
```

The EffectPipeline can freely intercept the verify or / and advance while also accessing the state and context at will by specifying type bounderies. Here we filter for contexts that have the same replicaId than in the state. Its worth noting that we are using an `verifyEffectPipeline` helper to simplify the creation of verify only extensions.

```scala
trait SingleOwnerStateExtension:
  val replicaId: String

object SingleOwnerEffectPipeline:
  def apply[A <: SingleOwnerStateExtension, C <: IdentityContext with IdentityContext](using Crypt): EffectPipeline[A, C] =
    verifyEffectPipeline[A, C]((state, context) => Set(
      Option.unless(state.replicaId == context.replicaId)
        (s"Replica ${context.replicaId} is not the owner ${state.replicaId} of this object.")
    ))
```

## Flow
When a user wants to change the State, he creates an Event with an associated context. This Event is then prepared by the ECmRDT to an EventWrapper containing additional distribution information about the vector clock. This EventWrapper can then be applied to an ECmRDT by the effect method. Internally effect will verify the wrapper itself and pass it into the EffectPipeline to apply the extension pipeline. The result can then verify and advance the state using the given context. After advancing the state the vector clock is updated.

## Counter example without extensions
First we need to define our State and Context. Every Context needs to have the IdentityContext trait. 

```
case class Counter(val value: Int,) 
case class CounterContext(val replicaId: String) extends IdentityContext
```

Because we do not define any Extensions we define an empty EffectPipeline. 

```scala
object Counter:
  given (using Crypt): EffectPipeline[Counter, CounterContext] = EffectPipeline()
```

We have an single Event where our verification is that we want the value to be positive.

```scala
case class AddCounterEvent(val value: Int) extends Event[Counter, CounterContext]:
  def asEffect: Effect[Counter, CounterContext] =
    Effect.from(
      (state, context) => Option.when(value < 0)(s"Value must be positive."),
      (state, context) => state.copy(value = state.value + value)
    )
```

We could additionally define an constructor for our AddEventCounter but with these definitions we could already start creating our ECmRDT.

```scala
val counter = ECmRDT[Counter, CounterContext](Counter(0))
```

## Claims


## Asymmetric Permission Extension

