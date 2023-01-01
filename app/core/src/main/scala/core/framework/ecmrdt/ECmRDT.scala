package core.framework.ecmrdt

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

case class Effect[A, C](
  val verify:  (A, C) => Future[Option[String]],
  val advance: (A, C) => Future[A]
)

object Effect:
  def apply[A, C](verify: (A, C) => Future[Option[String]], advance: (A, C) => Future[A]): Effect[A, C] =
    new Effect(verify, advance)

  def from[A, C](verify: (A, C) => Option[String], advance: (A, C) => A): Effect[A, C] =
    new Effect((a, c) => Future.successful(verify(a, c)), (a, c) => Future.successful(advance(a, c)))

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
def verifyEffectPipeline[A, C](f: (A, C) => Set[Option[String]]): Effect[A, C] => Effect[A, C] =
  verifyEffectPipelineFuture((a, c) => f(a, c).map(Future.successful(_)))

// Helper to build a asynchronous verify only effect pipelines
def verifyEffectPipelineFuture[A, C](f: (A, C) => Set[Future[Option[String]]]): Effect[A, C] => Effect[A, C] =
  (effect) =>
    Effect(
      (state, context) => 
        for
          coreValid <- Future.sequence(
            f(state, context)
          )

          valid <- if coreValid.forall(_.isEmpty) then 
            effect.verify(state, context)
          else
            Future.successful(Some(coreValid.flatten.mkString(" ")))

        yield
          valid,
      effect.advance
    )

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
  def prepare(eventWithContext: EventWithContext[A, C, E])(using effectPipeline: EffectPipeline[A, C]) =
    val effect = effectPipeline(eventWithContext.event.asEffect)

    for
      valid <- effect.verify(state, eventWithContext.context)
    yield
      valid match
        case Some(error) => Left(error)
        case None => Right(
          ECmRDTEventWrapper(eventWithContext, clock(eventWithContext.context.replicaId))
        )

  def effect(wrapper: ECmRDTEventWrapper[A, C, E])(using effectPipeline: EffectPipeline[A, C]): Future[Either[String, ECmRDT[A, C, E]]] =
    val effect = effectPipeline(wrapper.eventWithContext.event.asEffect)

    println(s"Clocks: ${clock.times}, ${wrapper.time}")

    if !clock.verify(wrapper.eventWithContext.context.replicaId, wrapper.time) then
      return Future.successful(Left(s"Invalid vector clock, expected ${clock(wrapper.eventWithContext.context.replicaId)} but got ${wrapper.time}."))

    for
      valid <- effect.verify(state, wrapper.eventWithContext.context)

      newState <- valid match
        case Some(error) => Future.successful(Left(error))
        case None => effect.advance(state, wrapper.eventWithContext.context).map(Right(_))
    yield
      newState.map(x => copy(
        state = x,
        clock = clock.next(wrapper.eventWithContext.context.replicaId)
      ))

object ECmRDT:
  given [A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C]]: JsonValueCodec[ECmRDT[A, C, E]] = JsonCodecMaker.make

// For testing prepare and effect event
def testingPrepareAndEffect[A, C <: IdentityContext, E <: Event[A, C]](ecmrdt: ECmRDT[A, C, E], event: EventWithContext[A, C, E])(using EffectPipeline[A, C]) =
  for
    prepared <- ecmrdt.prepare(event)
    
    newCounter <- prepared match
      case Left(error) => Future.successful(Left(error))
      case Right(prepared) => ecmrdt.effect(prepared)
  yield
    newCounter
    
trait IdentityContext:
  val replicaId: ReplicaId
