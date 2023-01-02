package webapp.state.framework

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.framework.ecmrdt.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

case class EventBufferContainer[A, C <: IdentityContext, E <: Event[A, C]](
  val inner: ECmRDT[A, C, E],
  val events: Set[ECmRDTEventWrapper[A, C, E]] = Set[ECmRDTEventWrapper[A, C, E]](),
):
  def effectPrepared(eventPrepared: ECmRDTEventWrapper[A, C, E])(using EffectPipeline[A, C]): Future[Either[String, EventBufferContainer[A, C, E]]] =
    inner.effect(eventPrepared)
      .map(_.map(newInner =>
        EventBufferContainer(
          inner = newInner,
          events = events + eventPrepared
        )
      ))

  def effect(event: EventWithContext[A, C, E])(using EffectPipeline[A, C]): Future[Either[String, EventBufferContainer[A, C, E]]] =
    // Prepare event
    inner.prepare(event)
      .flatMap {
        case Left(message) => 
          Future.successful(Left(message))

        // If successfull
        case Right(prepared) => 
          // Effect event
          inner.effect(prepared)
            // If successfull
            .map(_.map(newInner =>
              // Return new container
              EventBufferContainer(
                inner = newInner,
                events = events + prepared
              )
            ))
      }
  
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
