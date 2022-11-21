package functions.device.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import concurrent.ExecutionContext.Implicits.global
import core.messages.common.*
import functions.*
import functions.services.*
import functions.state.*
import functions.state.framework.*
import kofre.base.*
import scala.concurrent.*
import scala.reflect.Selectable.*
import scala.scalajs.js
import typings.azureCosmos.mod.*

trait StorageContainerInterface:
  def put[A : JsonValueCodec](name: String, id: String)(value: A): Future[Unit]
  def get[A : JsonValueCodec](id: String): Future[Option[A]]

trait StorageServiceInterface:
  def container(name: String): StorageContainerInterface

class StorageContainer(
  name: String,
  dbFuture: Future[Database],
  services: Services
) extends StorageContainerInterface:
  val container = dbFuture
    .flatMap(db =>
      services.logger.trace(s"Create Container: $name")

      db.containers.createIfNotExists(
        ContainerRequest()
          .setId(name)
          .setPartitionKey("/id")
      ).toFuture
    )
    .map(response =>
      services.logger.trace(s"AggregateRepository.create, Created Container: $name - ${response.statusCode}")

      response
        .ensureSuccessfullStatusCode(s"AggregateRepository.create, Create container for type $name: ")
        .container
    )
  
  def put[A : JsonValueCodec](name: String, id: String)(value: A): Future[Unit] =
    container.flatMap(container =>
      container.items.upsert(JsAggregateContainer(
        id,
        writeToString(value)
      )).toFuture
    )
      .map(_.ensureSuccessfullStatusCode(s"AggregateRepository.applyDelta, Set aggregate with id $id: "))
      .map(_ => ())

  def get[A : JsonValueCodec](id: String): Future[Option[A]] =
    container
      .flatMap(
        _.item(id, id).read[JsAggregateContainer]().toFuture
      )
      .map(response =>
        response.statusCode match
          case 404 =>
            services.logger.trace(s"AggregateRepository.get, Aggregate not found: $name - ${id}")
            None

          case _ =>
            response.ensureSuccessfullStatusCode(s"AggregateRepository.get, Get aggregate $name with id $id: ")

            // Assuming resource contains aggregate if response code is successfull
            Some(readFromString(response.resource.get.aggregateJson))
      )

  class JsAggregateContainer(
    // This property must be named id for CosmosDB to work
    // https://stackoverflow.com/questions/48641133/can-i-create-azure-cosmos-db-document-with-a-custom-key-other-than-id
    val id: String,
    val aggregateJson: String

  ) extends js.Object

class StorageService(
  services: Services,
  context: js.Dynamic
) extends StorageServiceInterface:
  val cosmosClient = CosmosClient(services.config.cosmosDBConnectionString)

  val database = cosmosClient.databases
    .createIfNotExists(
      DatabaseRequest().setId("Core")
    )
    .toFuture
    .map(_.ensureSuccessfullStatusCode("AggregateRepository.init, Create Database: ")    )
    .map(_.database)

  val openContainers = Map[String, StorageContainerInterface]()

  def container(name: String): StorageContainerInterface =
    openContainers.getOrElse(name, StorageContainer(name, database, services))

extension [A, B <: ResourceResponse[A]](response: B)
  def ensureSuccessfullStatusCode(message: => String) =
    if response.statusCode != 200 && response.statusCode != 201 then
      throw new Exception(s"$message Unexpected status code ${response.statusCode}")

    response
