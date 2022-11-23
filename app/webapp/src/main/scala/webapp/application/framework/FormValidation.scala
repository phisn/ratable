package webapp.application.framework

import rescala.default.*

enum ValidationState:
  case None, Error

class FormValidation:  // Combination is done with & instead of && to avoid short-circuiting
  private val validators = collection.mutable.Set[() => Boolean]()
  
  def validate: Boolean =
    // Run all validators without short-circuiting
    validators.foldLeft(true) { (acc, validator) =>
      acc & validator()
    }

  def validateVar[A](default: A, validator: A => Boolean): VarWithValidation[A] =
    validateVar(Var(default), validator)

  def validateVar[A](default: Var[A], validator: A => Boolean): VarWithValidation[A] =
    val value = default
    val state = Var(ValidationState.None)

    validators += (() =>
      val valid = validator(value.now)

      state.set(
        if valid then ValidationState.None
        else ValidationState.Error
      )

      valid
    )

    VarWithValidation(value, state)

