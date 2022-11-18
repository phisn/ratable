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

import core.messages.common.*
import core.messages.http.*
import typings.std.global.TextEncoder
import scala.scalajs.js.typedarray.Int8Array
import core.framework.TaggedDelta
import scala.util.Failure

class FacadeRepositoryFactory(services: {
  val config: ApplicationConfigInterface
  val logger: LoggerServiceInterface
  val statePersistence: StatePersistenceServiceInterface
  val stateDistribution: StateDistributionServiceInterface
  val aggregateFactory: AggregateFactory
}):
  def registerAggregateAsRepository[A : JsonValueCodec : Bottom : Lattice](
    aggregateType: AggregateType
  ): FacadeRepository[A] = 
    services.statePersistence.migrationForRepository(aggregateType)

    val facades = collection.mutable.Map[String, Facade[A]]()

    def tryLoadAggregate(id: String) =
      val actions = Evt[A => A]()
        
      val facadeInFuture = services.statePersistence
        .loadAggregate(AggregateGid(id, aggregateType))

        // For testing: Load aggregate from server if not found in local storage
        .flatMap {
          case Some(value) => Future.successful(Some(value))
          case None => 
            import sttp.client3.*

            val backend = FetchBackend()

            basicRequest.post(uri"${services.config.backendUrl}http")
              .contentType("application/x-protobuf")
              .body(ClientHttpMessage(ClientHttpMessage.Message.GetAggregate(
                GetAggregateMessage(AggregateGid(id, aggregateType))
              )).toByteArray)
              .response(asByteArray)
              .send(backend)
              .map(_.body.toOption)
              .map(_.flatMap(body =>
                RespondAggregateMessage.validate(body) match
                  case Success(message) => message.aggregateJson match
                    case Some(json) => 
                      services.logger.log(s"Loaded aggregate from server for $id as ${TextEncoder().encode(message.aggregateJson.get).toArray.fold("")((acc, b) => f"$acc ${b.asInstanceOf[Short]}%X")}")
                      Some(readFromString[A](json))
                      
                    case None => 
                      services.logger.warning(s"Aggregate not found on server: $id")
                      None
                  
                  case Failure(exception) => 
                    services.logger.error(s"Failed to load aggregate from server for $id ${exception.getMessage}")
                    None
              ))
              .map(_.map(DeltaContainer(_)))
        }

        .map(_.map(aggregate =>
          Facade(
            actions,
            services.aggregateFactory.createAggregateSignal
              (actions, AggregateGid(id, aggregateType))
              (aggregate).map(_.inner),
          )
        ))

      actions.recoverEventsUntilCompleted(facadeInFuture)

      facadeInFuture
        .andThen {
          case Success(Some(facade)) => facades.put(id, facade)
          case _ =>
        }
  
    new FacadeRepository:
      def get(id: String): Future[Option[Facade[A]]] =
        facades.get(id) match
          case Some(facade) => Future.successful(Some(facade))
          case None => 
            tryLoadAggregate(id)

      def create(id: String, aggregate: A): Future[Unit] =
        val actions = Evt[A => A]()

        val facade = Facade(
          actions, 
          services.aggregateFactory.createAggregateSignal
            (actions, AggregateGid(id, aggregateType))
            (DeltaContainer(aggregate)).map(_.inner)
        )

        facades += id -> facade

        // Explicitly save and distribute the aggregate, because they are usally done by action handling
        services.stateDistribution.pushDelta(AggregateGid(id, aggregateType), TaggedDelta(0, aggregate))
        services.statePersistence.saveAggregate(AggregateGid(id, aggregateType), DeltaContainer(aggregate))
