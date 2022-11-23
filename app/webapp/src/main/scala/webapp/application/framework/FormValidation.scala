package webapp.application.framework

import rescala.default.*

enum ValidationState:
  case None, Error

class FormValidation(
  val aggregation: Evt[Unit => Boolean] = Evt()
):
  // Combination is done with & instead of && to avoid short-circuiting
  private val validator = aggregation.fold[Unit => Boolean](_ => true)((a, b) => _ => a(()) & b(()))
  
  def validate: Boolean =
    val helper: Unit => Boolean = validator.now
    helper(())

  def validate[A](default: A, validator: A => Boolean): VarWithValidation[A] =
    validate(Var(default), validator)

  def validate[A](default: Var[A], validator: A => Boolean): VarWithValidation[A] =
    val value = default
    val state = Var(ValidationState.None)

    aggregation.fire(_ =>
      val valid = validator(value.now)

      state.set(
        if valid then ValidationState.None
        else ValidationState.Error
      )

      valid
    )

    VarWithValidation(value, state)

