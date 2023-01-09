package webapp.state.framework

import cats.data.*
import core.framework.*
import core.framework.ecmrdt.*
import rescala.default.{Signal, Var}
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.state.framework.*

class AggregateFacade[A, C <: IdentityContext, E <: Event[A, C]](
  private val initial: EventBufferContainer[A, C, E]
):
  private val variable = Var(initial)
  private var aggregateInFuture = Future.successful(initial)

  def listen: Signal[EventBufferContainer[A, C, E]] = variable

  def mutateTrivial(f: EventBufferContainer[A, C, E] => EventBufferContainer[A, C, E]): EitherT[Future, RatableError, EventBufferContainer[A, C, E]] =
    mutate(aggregate => EitherT.rightT(f(aggregate)))

  def mutate(f: EventBufferContainer[A, C, E] => EitherT[Future, RatableError, EventBufferContainer[A, C, E]]): EitherT[Future, RatableError, EventBufferContainer[A, C, E]] =
    // Using mutation attempt because our function has two outputs
    // 1. The result of f to the caller
    // 2. On success the new aggregate value to be used by others as a change in variable
    case class MutationAttempt(
      val aggregate: EventBufferContainer[A, C, E],
      val error: Option[RatableError]
    )

    // Aggregate is some value that will be there at *some* time in the future. We want to use this value
    // Because our operation can fail, we need to return the previous value if it failed
    val mutationAttempt = 
      for
        aggregate <- aggregateInFuture
        newAggregate <- f(aggregate).value
      yield
        newAggregate match
          case Right(newAggregate) =>
            variable.set(newAggregate)
            MutationAttempt(newAggregate, None)

          case Left(error) =>
            MutationAttempt(aggregate, Some(error))

    // After we used this value, we mutate aggregate to a new value in the future. So others
    // can use this value mutated by *us* in the future 
    aggregateInFuture = mutationAttempt.map {
      case MutationAttempt(newAggregate, _) => newAggregate
    }

    // We return our mutation if it was successful or an error if it was not
    EitherT(mutationAttempt.map {
      case MutationAttempt(newAggregate, None) => Right(newAggregate)
      case MutationAttempt(_, Some(error)) => Left(error)
    })
