package functions.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import concurrent.ExecutionContext.Implicits.global
import core.messages.common.*
import functions.services.*
import functions.state.*
import functions.state.framework.*
import scala.concurrent.*
import scala.reflect.Selectable.*
import scala.scalajs.js
import typings.azureCosmos.mod.ContainerRequest
import typings.azureCosmos.mod.CosmosClient
import typings.azureCosmos.mod.DatabaseRequest
import typings.azureCosmos.mod.ResourceResponse

trait AggregateRepositoryFactoryInterface:
  def create[A : JsonValueCodec](aggregateType: AggregateType): AggregateRepository[A]

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
  ).toFuture.map(mapResourceResponse(_, "Create Database: ").database)

  def create[A : JsonValueCodec](aggregateType: AggregateType): AggregateRepository[A] =
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
        mapResourceResponse(response, s"Create container for type ${aggregateType.name}: ").container
      )

    new AggregateRepository:
      override def get(id: String): Future[Option[A]] =
        services.logger.trace(s"Get Aggregate: ${aggregateType.name} - ${id}")

        container.flatMap(ct =>
          ct.item(id, id).read[JsAggregateContainer]().toFuture
        )
        // TODO: Not clean refactor
        .map(response =>
          services.logger.trace(s"Got Aggregate: ${response.item.id}, ${response.resource}, ${aggregateType.name} - ${response.statusCode}")

          if response.statusCode == 404 then
            services.logger.trace(s"Aggregate not found: ${aggregateType.name} ${id}")
            None
          else
            val resource = mapResourceResponse(response, s"Get aggregate ${aggregateType.name} with id $id: ")
              .resource.toOption.get

            Some(
              readFromString(resource.aggregateJson)
            )
        )

      override def set(id: String, aggregate: A): Future[Unit] =
        services.logger.trace(s"Set aggregate: ${aggregateType.name} ${id}")

        container
          .flatMap(
            _.items.upsert(JsAggregateContainer(
              id,
              writeToString(aggregate)
            )).toFuture
          )
          .map(response => mapResourceResponse(response, s"Set aggregate with id $id: "))
          .map(_ => ())

  def mapResourceResponse[A, B <: ResourceResponse[A]](response: B, message: => String) =
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
