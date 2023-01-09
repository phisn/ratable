package webapp.state.services

import cats.data.*
import cats.effect.*
import cats.implicits.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.*
import core.framework.ecmrdt.*
import core.messages.common.AggregateGid
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import scala.util.*
import webapp.services.*
import webapp.state.framework.*

class AggregateViewProvider(services: {
  val aggregateFacadeProvider: AggregateFacadeProvider
  val logger: LoggerServiceInterface
  val stateDistribution: StateDistributionService
}):
  def create[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec]
    (gid: AggregateGid, initial: A)(using Crypt): EitherT[Future, RatableError, AggregateView[A, C, E]] =
    services.aggregateFacadeProvider.create[A, C, E](gid, initial).map(facadeToView(gid))

  def get[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec]
    (gid: AggregateGid)(using Crypt): EitherT[Future, RatableError, Option[AggregateView[A, C, E]]] =
    for
      facade <- services.aggregateFacadeProvider.get[A, C, E](gid)
    yield
      facade.map(facadeToView(gid))

  private def facadeToView[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec]
    (gid: AggregateGid)(facade: AggregateFacade[A, C, E])(using Crypt): AggregateView[A, C, E] =
    new AggregateView:
      def listen: rescala.default.Signal[A] = 
        facade.listen.map(_.inner.state)
      
      def effect(event: EventWithContext[A, C, E])(using EffectPipeline[A, C]): EitherT[Future, RatableError, Unit] =
        val x: EitherT[Future, RatableError, EventBufferContainer[A, C, E]] = facade.mutate(aggregate => aggregate.effect(event))

        x.value.andThen {
          case Success(Right(container)) =>
            services.stateDistribution.distribute(gid, container)
        }

        x.map(_ => ())
