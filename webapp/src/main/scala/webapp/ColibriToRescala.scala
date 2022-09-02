package webapp

import colibri.{Cancelable, Observer, Sink}
import rescala.default.*
import rescala.operator.Pulse

given (using scheduler: Scheduler): Sink[Evt] with
  def unsafeOnNext[A](sink: Evt[A])(value: A): Unit = sink.fire(value)
  def unsafeOnError[A](sink: Evt[A])(error: Throwable): Unit = scheduler.forceNewTransaction(sink) { implicit turn => sink.admitPulse(Pulse.Exceptional(error)) }

given (using scheduler: Scheduler): Sink[Var] with
  def unsafeOnNext[A](sink: Var[A])(value: A): Unit = sink.set(value)
  def unsafeOnError[A](sink: Var[A])(error: Throwable): Unit = scheduler.forceNewTransaction(sink) { implicit turn => sink.admitPulse(Pulse.Exceptional(error)) }

given colibri.Source[Event] with
  def unsafeSubscribe[A](stream: Event[A])(sink: Observer[A]): Cancelable =
    val sub = stream.observe(sink.unsafeOnNext, sink.unsafeOnError)
    Cancelable(sub.remove)

given colibri.Source[Signal] with
  def unsafeSubscribe[A](stream: Signal[A])(sink: colibri.Observer[A]): colibri.Cancelable =
    val sub = stream.observe(sink.unsafeOnNext, sink.unsafeOnError)
    colibri.Cancelable(sub.remove)