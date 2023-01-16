package functions.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import concurrent.ExecutionContext.Implicits.global
import core.messages.common.*
import functions.device.services.*
import functions.services.*
import functions.state.*
import functions.state.framework.*
import kofre.base.*
import scala.concurrent.*
import scala.reflect.Selectable.*
import scala.scalajs.js

trait AggregateRepositoryFactoryInterface:
  def create[A : Bottom : Lattice : JsonValueCodec](aggregateType: AggregateType): AggregateRepository[A]

class AggregateRepositoryFactory(
  services: {
    val config: ApplicationConfig
    val logger: LoggerServiceInterface
    val storage: StorageServiceInterface
  },
  context: js.Dynamic
) extends AggregateRepositoryFactoryInterface:
  def create[A : Bottom : Lattice : JsonValueCodec](aggregateType: AggregateType): AggregateRepository[A] =
    val lowercase = aggregateType.name.toLowerCase()

    new AggregateRepository:
      override def get(id: String): Future[Option[A]] =
        services.logger.trace(s"Get Aggregate: ${aggregateType.name} - ${id}")
        services.storage.container(lowercase).get(id)

      override def applyDelta(id: String, delta: A): Future[Unit] =
        services.logger.trace(s"AggregateRepository.applyDelta, Set aggregate: ${aggregateType.name} ${id}")

        get(id)
          .map(_.getOrElse(Bottom[A].empty))
          .map(Lattice[A].merge(_, delta))
          .flatMap(services.storage.container(lowercase).put(lowercase, id))
