package webapp.mocks

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.state.framework.*
import kofre.base.*
import org.scalajs.dom
import rescala.default.*
import scala.collection.mutable.*
import scala.collection.immutable.Set
import scala.concurrent.*
import webapp.services.*
import webapp.state.framework.*
import webapp.state.services.*

class StatePersistenceServiceMock(
  val aggregates: collection.mutable.Map[(String, String), DeltaContainer[_]] = Map.empty
) extends StatePersistenceServiceInterface:
  val saves = collection.mutable.Buffer[(String, String, DeltaContainer[_])]()
  val migrations = collection.mutable.Buffer[String]()

  def saveAggregate[A : JsonValueCodec](aggregateTypeId: String, id: String, aggregate: DeltaContainer[A]): Future[Unit] =
    saves.append((aggregateTypeId, id, aggregate.asInstanceOf[DeltaContainer[_]]))
    aggregates += (aggregateTypeId, id) -> aggregate
    Future.successful(())

  def loadAggregate[A : JsonValueCodec](aggregateTypeId: String, id: String): Future[Option[DeltaContainer[A]]] =
    Future.successful(aggregates.get((aggregateTypeId, id)).map(_.asInstanceOf[DeltaContainer[A]]))

  def deleteAggregate[A : JsonValueCodec](aggregateTypeId: String, id: String): Unit =
    ()

  def migrationForRepository(aggregateTypeId: String): Unit =
    migrations.append(aggregateTypeId)

  def boot: Unit =
    ()
