package webapp.application.framework


import colibri.{Cancelable, Observer, Sink}
import rescala.default.*
import rescala.operator.Pulse

class PromiseSignal[A] private (val default: Option[A]):
  private val source: Var[Signal[A]] = default.map(x => Var(Signal(x))).getOrElse(Var.empty)
  private var flattend = source.flatten
  
  inline def :=(inline expr: A) =
    flattend = source.flatten
    source.set(Signal.dynamic(expr))

object PromiseSignal:
  def apply[A](): PromiseSignal[A] =
    new PromiseSignal[A](None)

  def apply[A](initial: A): PromiseSignal[A] =
    new PromiseSignal[A](Some(initial))

  given colibri.Source[PromiseSignal] with
    def unsafeSubscribe[A](stream: PromiseSignal[A])(sink: colibri.Observer[A]): colibri.Cancelable =
      val sub = stream.observe(sink.unsafeOnNext, sink.unsafeOnError)
      colibri.Cancelable(sub.disconnect)

  implicit def asSignal[A](promise: PromiseSignal[A]): Signal[A] =
    promise.flattend
