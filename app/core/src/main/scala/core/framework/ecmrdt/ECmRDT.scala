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

trait Event[A, C]:
  def asEffect: Effect[A, C]

case class EventWithContext[A, C, +E <: Event[A, C]](
  val event: E,
  val context: C
)

object EventWithContext:
  given [A : JsonValueCodec, C : JsonValueCodec, E <: Event[A, C] : JsonValueCodec]: JsonValueCodec[EventWithContext[A, C, E]] = JsonCodecMaker.make

type Effect[A, C] = (A, C) => EitherT[Future, RatableError, A]

/*
object Effect:
  def apply[A, C](verify: (A, C) => OptionT[Future, String], advance: (A, C) => Future[A]): Effect[A, C] =
    new Effect(verify, advance)

  def from[A, C](verify: (A, C) => Option[String], advance: (A, C) => A): Effect[A, C] =
    new Effect((a, c) => Future.successful(verify(a, c)), (a, c) => Future.successful(advance(a, c)))
*/

trait EffectPipeline[A, C]:
  def apply(effect: Effect[A, C]): Effect[A, C]

object EffectPipeline:
  // Allow for implicit conversion from a function to an EffectPipeline. Needed because helper functions
  // usally return a function, which is not automatically converted to an EffectPipeline.
  given [A, C]: Conversion[Effect[A, C] => Effect[A, C], EffectPipeline[A, C]] with
    def apply(f: Effect[A, C] => Effect[A, C]): EffectPipeline[A, C] =
      (effect: Effect[A, C]) => f(effect)

  def apply[A, C](stages: EffectPipeline[A, C]*): EffectPipeline[A, C] =
    effect => stages.foldLeft(effect)((x, y) => y.apply(x))

// Helper to build a synchronous verify only effect pipelines
def verifyEffectPipeline[A, C](f: (A, C) => List[Option[RatableError]]): Effect[A, C] => Effect[A, C] =
  verifyEffectPipelineFuture((a, c) => f(a, c).map(OptionT.fromOption(_)))

// Helper to build a asynchronous verify only effect pipelines
def verifyEffectPipelineFuture[A, C](f: (A, C) => List[OptionT[Future, RatableError]]): Effect[A, C] => Effect[A, C] =
  (effect) =>
    (state, context) => 
      for
        valid <- f(state, context).sequence.map(
          _.foldLeft(RatableError())(_.combine(_))
        ).toLeft(())

        newState <- effect(state, context)

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

case class ECmRDTEventWrapper[A, C, E <: Event[A, C]](
  val eventWithContext: EventWithContext[A, C, E],
  val time: Long
)

object ECmRDTEventWrapper:
  given [A : JsonValueCodec, C : JsonValueCodec, E <: Event[A, C] : JsonValueCodec]: JsonValueCodec[ECmRDTEventWrapper[A, C, E]] = JsonCodecMaker.make

case class ECmRDT[A, C <: IdentityContext, E <: Event[A, C]](
  val state: A,
  val clock: VectorClock = VectorClock(Map.empty)
):
  def prepare(eventWithContext: EventWithContext[A, C, E])(using effectPipeline: EffectPipeline[A, C]): ECmRDTEventWrapper[A, C, E] =
    ECmRDTEventWrapper(eventWithContext, clock(eventWithContext.context.replicaId))

  def effect(wrapper: ECmRDTEventWrapper[A, C, E])(using effectPipeline: EffectPipeline[A, C]): EitherT[Future, RatableError, ECmRDT[A, C, E]] =
    val effect = effectPipeline(wrapper.eventWithContext.event.asEffect)

    println(s"Clocks: ${clock.times}, ${wrapper.time}")

    for
      _ <- EitherT.cond(
        clock.verify(wrapper.eventWithContext.context.replicaId, wrapper.time), (),
        RatableError(s"Invalid vector clock, expected ${clock(wrapper.eventWithContext.context.replicaId)} but got ${wrapper.time}.")
      )

      newState <- effect(state, wrapper.eventWithContext.context)
    yield
      copy(
        state = newState,
        clock = clock.next(wrapper.eventWithContext.context.replicaId)
      )

object ECmRDT:
  given [A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C]]: JsonValueCodec[ECmRDT[A, C, E]] = JsonCodecMaker.make

// For testing prepare and effect event
def testingPrepareAndEffect[A, C <: IdentityContext, E <: Event[A, C]](ecmrdt: ECmRDT[A, C, E], event: EventWithContext[A, C, E])(using EffectPipeline[A, C]) =
  for
    newCounter <- ecmrdt.effect(ecmrdt.prepare(event))
  yield
    newCounter
    
trait IdentityContext:
  val replicaId: ReplicaId
