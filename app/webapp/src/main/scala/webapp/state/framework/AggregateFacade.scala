package webapp.state.framework

import core.framework.*
import core.framework.ecmrdt.*
import rescala.default.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.state.framework.*

class AggregateFacade[A, C <: IdentityContext](
  private val initial: EventBufferContainer[A, C]
):
  private val variable = Var(initial)
  private var aggregateInFuture = Future.successful(initial)

  def view: AggregateView[A, C] =
    new AggregateView:
      def listen: Signal[A] = 
        variable.map(_.inner.state)
      
      def effect(event: EventWithContext[A, C])(using EffectPipeline[A, C]): Future[Option[String]] =
        AggregateFacade.this.effect(event)

  def listen: Signal[EventBufferContainer[A, C]] = variable

  def mutate(f: EventBufferContainer[A, C] => Future[Either[String, EventBufferContainer[A, C]]]): Future[Either[String, EventBufferContainer[A, C]]] =
    // Using mutation attempt because our function has two outputs
    // 1. The result of f to the caller
    // 2. On success the new aggregate value to be used by others as a change in variable
    case class MutationAttempt(
      val aggregate: EventBufferContainer[A, C],
      val error: Option[String]
    )

    // Aggregate is some value that will be there at *some* time in the future. We want to use this value
    // Because our operation can fail, we need to return the previous value if it failed
    val mutationAttempt = 
      for
        aggregate <- aggregateInFuture
        newAggregate <- f(aggregate)
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
    mutationAttempt.map {
      case MutationAttempt(newAggregate, None) => Right(newAggregate)
      case MutationAttempt(_, Some(error)) => Left(error)
    }

  def effect(event: EventWithContext[A, C])(using EffectPipeline[A, C]): Future[Option[String]] =
    mutate(aggregate => aggregate.effect(event)).map(_.left.toOption)
