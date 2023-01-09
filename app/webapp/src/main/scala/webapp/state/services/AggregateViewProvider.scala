package webapp.state.services

import cats.data.*
import cats.effect.*
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
    (gid: AggregateGid, initial: A)(using Crypt): Future[AggregateView[A, C, E]] =
    services.aggregateFacadeProvider.create[A, C, E](gid, initial).map(facadeToView(gid))

  def get[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec]
    (gid: AggregateGid)(using Crypt): Future[Option[AggregateView[A, C, E]]] =
    services.aggregateFacadeProvider.get[A, C, E](gid).map(_.map(facadeToView(gid)))

  private def facadeToView[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C] : JsonValueCodec]
    (gid: AggregateGid)(facade: AggregateFacade[A, C, E])(using Crypt): AggregateView[A, C, E] =
    new AggregateView:
      def listen: rescala.default.Signal[A] = 
        facade.listen.map(_.inner.state)
      
      def effect(event: EventWithContext[A, C, E])(using EffectPipeline[A, C]): OptionT[Future, RatableError] =
        val x = facade.mutate(aggregate => aggregate.effect(event))

        x.value.andThen {
          case Success(Right(container)) =>
            services.stateDistribution.distribute(gid, container)
        }

        x.swap.toOption
