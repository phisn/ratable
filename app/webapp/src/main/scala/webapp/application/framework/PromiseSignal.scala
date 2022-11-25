package webapp.application.framework


import colibri.{Cancelable, Observer, Sink}
import rescala.default.*
import rescala.operator.Pulse

class PromiseSignal[A] private (val default: Option[A]):
  private val source = default.map(Var(_)).getOrElse(Var.empty)
  
  inline def :=(inline expr: A) =
    Signal.dynamic(expr).observe(source.set)

object PromiseSignal:
  def apply[A](): PromiseSignal[A] =
    new PromiseSignal[A](None)

  def apply[A](initial: A): PromiseSignal[A] =
    new PromiseSignal[A](Some(initial))

  given (using scheduler: Scheduler): Sink[PromiseSignal] with
    def unsafeOnNext[A](sink: PromiseSignal[A])(value: A): Unit = sink.source.set(value)
    def unsafeOnError[A](sink: PromiseSignal[A])(error: Throwable): Unit = scheduler.forceNewTransaction(sink.source) { implicit turn => sink.source.admitPulse(Pulse.Exceptional(error)) }

  given colibri.Source[PromiseSignal] with
    def unsafeSubscribe[A](stream: PromiseSignal[A])(sink: colibri.Observer[A]): colibri.Cancelable =
      val sub = stream.observe(sink.unsafeOnNext, sink.unsafeOnError)
      colibri.Cancelable(sub.disconnect)

  implicit def asSignal[A](promise: PromiseSignal[A]): Signal[A] =
    promise.source
