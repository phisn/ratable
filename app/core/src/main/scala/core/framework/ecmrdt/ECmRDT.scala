package core.framework.ecmrdt

import cats.data.*
import cats.implicits._
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.framework.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import kofre.time.*
import scala.util.*

// An event is something that can be created by a user and applied to a state by
// converting it to an effect. An event is always associated with a context
trait Event[A, C]:
  def asEffect: Effect[A, C]

/*
// An event is usally associated with an context providing information used in
// effect pipelines
case class EventWithContext[A, C, +E <: Event[A, C]](
  val event: E,
  val context: C
)

object EventWithContext:
  given [A : JsonValueCodec, C : JsonValueCodec, E <: Event[A, C] : JsonValueCodec]: JsonValueCodec[EventWithContext[A, C, E]] = JsonCodecMaker.make
*/

// We need a meta to get access to information sorrounding our data type. 
// Especially needed in creation
case class MetaContext(
  val aggregateId: AggregateId,
  val ownerReplicaId: ReplicaId,
)

// An effect uses state, context, and meta to either mutate the state or fail if some
// security or other constraints are not met. This happens asynchronously because encryption
// and other operations might be involved.
type Effect[A, C] = (A, C, MetaContext) => EitherT[Future, RatableError, A]

// An effect pipeline is a function that takes an effect and returns an effect. It can be used
// to add additional security checks or other operations to effects of one data type.
trait EffectPipeline[A, C]:
  def apply(effect: Effect[A, C]): Effect[A, C]

object EffectPipeline:
  // Allow for implicit conversion from a function to an EffectPipeline. Needed because helper functions
  // usally return a function, which is not automatically converted to an EffectPipeline.
  given [A, C]: Conversion[Effect[A, C] => Effect[A, C], EffectPipeline[A, C]] with
    def apply(f: Effect[A, C] => Effect[A, C]): EffectPipeline[A, C] =
      (effect: Effect[A, C]) => f(effect)

  // Helper to use multiple effect pipelines in a row
  def apply[A, C](stages: EffectPipeline[A, C]*): EffectPipeline[A, C] =
    effect => stages.foldLeft(effect)((x, y) => y.apply(x))

// Helper to build a synchronous verify only effect pipelines
def verifyEffectPipeline[A, C](f: (A, C, MetaContext) => List[Option[RatableError]]): Effect[A, C] => Effect[A, C] =
  verifyEffectPipelineFuture((a, c, m) => f(a, c, m).map(OptionT.fromOption(_)))

// Helper to build a asynchronous verify only effect pipelines
def verifyEffectPipelineFuture[A, C](f: (A, C, MetaContext) => List[OptionT[Future, RatableError]]): Effect[A, C] => Effect[A, C] =
  (effect) =>
    (state, context, meta) => 
      for
        _ <- f(state, context, meta).map(_.toLeft(())).sequence
        newState <- effect(state, context, meta)

      yield
        newState

case class VectorClock(
  val times: Map[ReplicaId, Long]
):
  def apply(replicaId: ReplicaId): Long =
    times.getOrElse(replicaId, 0L)

  def next(replicaId: ReplicaId): VectorClock =
    copy(times = times.updated(replicaId, apply(replicaId) + 1))

  def verify(replicaId: ReplicaId, time: Long): Boolean =
    apply(replicaId) == time

// An event packaged with its context and additional information. The way it will
// be distributed and stored
case class ECmRDTEventWrapper[A, C, +E <: Event[A, C]](
  val time: Long,
  val event: E,
  val context: C,
)

object ECmRDTEventWrapper:
  given [A : JsonValueCodec, C : JsonValueCodec, E <: Event[A, C] : JsonValueCodec]: JsonValueCodec[ECmRDTEventWrapper[A, C, E]] = JsonCodecMaker.make

case class ECmRDT[A, C <: IdentityContext, E <: Event[A, C]](
  val state: A,
  val clock: VectorClock = VectorClock(Map.empty)
):
  def prepare(event: E, context: C)(using effectPipeline: EffectPipeline[A, C]): ECmRDTEventWrapper[A, C, E] =
    ECmRDTEventWrapper(
      clock(context.replicaId),
      event,
      context
    )

  def effect(wrapper: ECmRDTEventWrapper[A, C, E], meta: MetaContext)(using effectPipeline: EffectPipeline[A, C]): EitherT[Future, RatableError, ECmRDT[A, C, E]] =
    val effect = effectPipeline(wrapper.event.asEffect)

    println(s"Clocks: ${clock.times}, ${wrapper.time}")

    for
      _ <- EitherT.cond(
        clock.verify(wrapper.context.replicaId, wrapper.time), (),
        RatableError(s"Invalid vector clock, expected ${clock(wrapper.context.replicaId)} but got ${wrapper.time}.")
      )

      newState <- effect(state, wrapper.context, meta)
    yield
      copy(
        state = newState,
        clock = clock.next(wrapper.context.replicaId)
      )

object ECmRDT:
  given [A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C]]: JsonValueCodec[ECmRDT[A, C, E]] = JsonCodecMaker.make
    
// A context providing information about the user that created the event
trait IdentityContext:
  val replicaId: ReplicaId

trait InitialECmRDT[A]:
  def value: A

object InitialECmRDT:
  def apply[A](initial: A): InitialECmRDT[A] =
    new InitialECmRDT[A]:
      def value: A = initial
