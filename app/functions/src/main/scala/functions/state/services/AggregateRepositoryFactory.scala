package functions.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import concurrent.ExecutionContext.Implicits.global
import core.messages.common.*
import functions.services.*
import functions.state.*
import functions.state.framework.*
import kofre.base.*
import scala.concurrent.*
import scala.reflect.Selectable.*
import scala.scalajs.js
import typings.azureCosmos.mod.ContainerRequest
import typings.azureCosmos.mod.CosmosClient
import typings.azureCosmos.mod.DatabaseRequest
import typings.azureCosmos.mod.ResourceResponse

trait AggregateRepositoryFactoryInterface:
  def create[A : Bottom : Lattice : JsonValueCodec](aggregateType: AggregateType): AggregateRepository[A]

class AggregateRepositoryFactory(
  services: {
    val config: ApplicationConfig
    val logger: LoggerServiceInterface
  },
  context: js.Dynamic
) extends AggregateRepositoryFactoryInterface:
  val cosmosClient = CosmosClient(services.config.cosmosDBConnectionString)

  val database = cosmosClient.databases.createIfNotExists(
    DatabaseRequest().setId("Core")
  ).toFuture.map(_.ensureSuccessfullStatusCode("Create Database: ").database)

  def create[A : Bottom : Lattice : JsonValueCodec](aggregateType: AggregateType): AggregateRepository[A] =
    val lowercase = aggregateType.name.toLowerCase()

    val container = database
      .flatMap(db =>
        services.logger.trace(s"Create Container: ${aggregateType.name}")

        db.containers.createIfNotExists(
          ContainerRequest()
            .setId(lowercase)
            .setPartitionKey("/id")
        ).toFuture
      )
      .map(response =>
        services.logger.trace(s"Created Container: ${aggregateType.name} - ${response.statusCode}")

        response
          .ensureSuccessfullStatusCode(s"Create container for type ${aggregateType.name}: ")
          .container
      )

    new AggregateRepository:
      override def get(id: String): Future[Option[A]] =
        services.logger.trace(s"Get Aggregate: ${aggregateType.name} - ${id}")

        container
          .flatMap(
            _.item(id, id).read[JsAggregateContainer]().toFuture
          )
          .map(response =>
            response.statusCode match
              case 404 =>
                services.logger.trace(s"Aggregate not found: ${aggregateType.name} - ${id}")
                None

              case _ =>
                response.ensureSuccessfullStatusCode(s"Get aggregate ${aggregateType.name} with id $id: ")

                // Assuming resource contains aggregate if response code is successfull
                Some(readFromString(response.resource.get.aggregateJson))
          )

      override def applyDelta(id: String, delta: A): Future[Unit] =
        services.logger.trace(s"Set aggregate: ${aggregateType.name} ${id}")

        get(id)
          .map(_.getOrElse(Bottom[A].empty))
          .zip(container)
          .flatMap((aggregate, container) =>
            container.items.upsert(JsAggregateContainer(
              id,
              writeToString(Lattice[A].merge(aggregate, delta))
            )).toFuture
          )
          .map(_.ensureSuccessfullStatusCode(s"Set aggregate with id $id: "))
          .map(_ => ())

  extension [A, B <: ResourceResponse[A]](response: B)
    def ensureSuccessfullStatusCode(message: => String) =
      if response.statusCode != 200 && response.statusCode != 201 then
        services.logger.error(s"$message Unexpected status code ${response.statusCode}")
        throw new Exception(s"$message Unexpected status code ${response.statusCode}")

      services.logger.trace(s"$message ${response.statusCode}")

      response
  
  class JsAggregateContainer(
    // This property must be named id for CosmosDB to work
    // https://stackoverflow.com/questions/48641133/can-i-create-azure-cosmos-db-document-with-a-custom-key-other-than-id
    val id: String,
    val aggregateJson: String

  ) extends js.Object
