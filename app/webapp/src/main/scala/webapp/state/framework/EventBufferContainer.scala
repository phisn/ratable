package webapp.state.framework

import cats.data.*
import cats.implicits.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.framework.*
import core.framework.ecmrdt.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

case class EventBufferContainer[A, C <: IdentityContext, E <: Event[A, C]](
  val inner: ECmRDT[A, C, E],
  val events: List[ECmRDTEventWrapper[A, C, E]] = List[ECmRDTEventWrapper[A, C, E]](),
):
  def effectPrepared(eventPrepared: ECmRDTEventWrapper[A, C, E], meta: MetaContext)(using EffectPipeline[A, C]): EitherT[Future, RatableError, EventBufferContainer[A, C, E]] =
    for
      newInner <- inner.effect(eventPrepared, meta)
    yield
      EventBufferContainer(
        inner = newInner,
        events = eventPrepared :: events
      )

  def effect(event: E, context: C, meta: MetaContext)(using EffectPipeline[A, C]): EitherT[Future, RatableError, EventBufferContainer[A, C, E]] =
    val prepared = inner.prepare(event, context)

    for
      newInner <- inner.effect(prepared, meta)

    yield
      EventBufferContainer(
        inner = newInner,
        events = prepared :: events
      )
  
  // Server responds with highest time to acknowledge events
  def acknowledge(time: Long) =
    EventBufferContainer(
      inner = inner,
      // All events with time higher than the acknowledged time remain
      events = events.filter(_.time > time)
    )

object EventBufferContainer:
  given [A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec]: JsonValueCodec[EventBufferContainer[A, C, E]] =
    JsonCodecMaker.make
