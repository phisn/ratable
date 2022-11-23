package webapp.application.framework

import rescala.default.*

case class VarWithValidation[A](
  val variable: Var[A],
  val state: Var[ValidationState]
)

object VarWithValidation:
  given [A]: Conversion[Var[A], VarWithValidation[A]] with
    def apply(variable: Var[A]): VarWithValidation[A] =
      VarWithValidation(variable, Var(ValidationState.None))
