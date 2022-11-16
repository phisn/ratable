package webapp.state.framework

import rescala.default.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

extension [A](evt: Evt[A])
  // Actions fired while future is not yet completed
  // will be replayed after the future is completed in the correct order
  def recoverEventsUntilCompleted(future: Future[_]) =
    val pending = collection.mutable.Queue[A]()
    
    evt
      .filter(_ => !future.isCompleted)
      .observe(pending.enqueue(_))

    future.onComplete(_ => pending
      // Dequeue not needed, because future will never be incomplete again
      //     .dequeueAll(_ => true)
      .foreach(evt.fire(_))
    )
