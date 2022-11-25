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

  def validatePromise[A](default: A, f: A => Boolean): PromiseSignalWithValidation[A] =
    validatePromise(PromiseSignal(default), f)

  def validatePromise[A](default: PromiseSignal[A], f: A => Boolean): PromiseSignalWithValidation[A] =
    val value = default
    val state = Var(ValidationState.None)

    val validator = () =>
      val valid = f(value.now)

      state.set(
        if valid then ValidationState.None
        else ValidationState.Error
      )

      valid

    validators += validator

    PromiseSignalWithValidation(value, state, () => validators -= validator)

