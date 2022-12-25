package webapp.state.framework

import core.framework.ecmrdt.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

case class EventBufferContainer[A, C <: IdentityContext](
  val inner: ECmRDT[A, C],
  val events: Set[ECmRDTEventWrapper[A, C]]
):
  def effect(eventPrepared: ECmRDTEventWrapper[A, C])(using EffectPipeline[A, C]): Future[Either[String, EventBufferContainer[A, C]]] =
    inner.effect(eventPrepared)
      .map(_.map(newInner =>
        EventBufferContainer(
          inner = newInner,
          events = events + eventPrepared
        )
      ))

  def effect(event: EventWithContext[A, C])(using EffectPipeline[A, C]): Future[Either[String, EventBufferContainer[A, C]]] =
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
