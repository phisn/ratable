package webapp.application.framework

import rescala.default.*

case class PromiseSignalWithValidation[A](
  val signal: PromiseSignal[A],
  val state: Signal[ValidationState],

  // This is a hack to allow the form to remove the validator when the component is removed
  val destroy: () => Unit
)

object PromiseSignalWithValidation:
  given [A]: Conversion[PromiseSignal[A], PromiseSignalWithValidation[A]] with
    def apply(variable: PromiseSignal[A]): PromiseSignalWithValidation[A] =
      PromiseSignalWithValidation(variable, Signal(ValidationState.None), () => ())
