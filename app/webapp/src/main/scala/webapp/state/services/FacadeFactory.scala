package webapp.state.services

import collection.immutable.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.messages.common.*
import kofre.base.*
import kofre.decompose.containers.*
import rescala.default.*
import rescala.operator.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import webapp.services.*
import webapp.state.framework.{*, given}
import webapp.state.{*, given}
import scala.util.Success

import core.framework.*
import core.messages.common.*
import core.messages.http.*
import core.messages.socket.*
import typings.std.global.TextEncoder
import scala.scalajs.js.typedarray.Int8Array
import core.framework.TaggedDelta
import scala.util.Failure
import webapp.device.services.*
import core.domain.aggregates.ratable.*
import webapp.device.storage.*
import typings.std.stdStrings.storage

class FacadeFactory(services: {
  val aggregateFactory: AggregateFactory
  val logger: LoggerServiceInterface
  val functionsHttpApi: FunctionsHttpApiInterface
}):
  def FacadeRepository[A : JsonValueCodec : Bottom : Lattice](
    aggregateType: AggregateType, 
    stateStorage: StateStorage
  ): FacadeRepository[A] =
    val facades = collection.mutable.Map[String, Facade[A]]()

    services.logger.trace(s"FacadeFactory.FacadeRepository: $aggregateType")

    new FacadeRepository:
      def create(id: String, aggregate: A): Unit =
        val facade = facadeFromInitial(
          AggregateGid(id, aggregateType), 
          stateStorage
        )(DeltaContainer(aggregate))

        facades += id -> facade
        facade.mutate(_ => aggregate)

      def get(id: String): Future[Option[Facade[A]]] =
        facades.get(id) match
          case Some(facade) => Future.successful(Some(facade))
          case None =>
            initialAggregate(AggregateGid(id, aggregateType), stateStorage)
              .map(_.map(
                facadeFromInitial(AggregateGid(id, aggregateType), stateStorage)
              ))
              .andThen {
                case Success(Some(facade)) => facades += id -> facade
              }

  def Facade[A : JsonValueCodec : Bottom : Lattice](
    aggregateType: AggregateType, 
    stateStorage: StateStorage
  ): Facade[A] =
    val gid = AggregateGid(aggregateType.name, aggregateType)

    val facade = initialAggregate(gid, stateStorage)
      .map(_.getOrElse(Bottom[DeltaContainer[A]].empty))
      .map(facadeFromInitial(gid, stateStorage))

    new Facade:
      def mutate(f: A => A) = facade.andThen {
        case Success(value) => value.mutate(f)
      }

      def listen = Signals.fromFuture(facade.map(_.listen)).flatten

  def initialAggregate[A : JsonValueCodec : Bottom : Lattice](
    gid: AggregateGid,
    stateStorage: StateStorage
  ) =
    stateStorage
      .load[A](gid)
      .flatMap {
        case Some(value) => Future.successful(Some(value))
        case None =>
          services.functionsHttpApi.getAggregate(
            GetAggregateMessage(gid)
          )
            .map(_.aggregateJson.map(readFromString[A](_)))
            .map(_.map(DeltaContainer(_)))
      }
      .andThen {
        case Failure(exception) =>
          services.logger.error(s"Failed to load aggregate $gid because ${exception}")
      }

  def facadeFromInitial[A : JsonValueCodec : Bottom : Lattice](gid: AggregateGid, stateStorage: StateStorage)(initial: DeltaContainer[A]): Facade[A] =
    val actions = Evt[A => A]()
    val signal = services.aggregateFactory.createSignal(actions, gid, stateStorage)(initial)

    new Facade:
      def mutate(f: A => A) = actions.fire(f)
      def listen = signal

