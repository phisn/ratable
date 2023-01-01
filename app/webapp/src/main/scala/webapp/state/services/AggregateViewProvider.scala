package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
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
  val stateDistributionService: StateDistributionService
  val logger: LoggerServiceInterface
}):
  def create[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C]](gid: AggregateGid, initial: A): Future[AggregateView[A, C, E]] =
    services.aggregateFacadeProvider.create[A, C, E](gid, initial).map(facadeToView(gid))

  def get[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C]](gid: AggregateGid): Future[Option[AggregateView[A, C, E]]] =
    services.aggregateFacadeProvider.get[A, C, E](gid).map(_.map(facadeToView(gid)))

  private def facadeToView[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C]](gid: AggregateGid)(facade: AggregateFacade[A, C, E]): AggregateView[A, C, E] =
    new AggregateView:
      def listen: rescala.default.Signal[A] = 
        facade.listen.map(_.inner.state)
      
      def effect(event: EventWithContext[A, C, E])(using EffectPipeline[A, C]): Future[Option[String]] =
        facade.mutate(aggregate => aggregate.effect(event))
          .andThen {
            case Success(Right(container)) =>
              // services.stateDistributionService.distribute(gid, container)
          }
          .map(_.left.toOption)
