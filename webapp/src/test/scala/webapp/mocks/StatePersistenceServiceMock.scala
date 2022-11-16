package webapp.mocks

import core.state.*
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
  val aggregates: collection.mutable.Map[AggregateId, DeltaContainer[_]] = Map.empty
) extends StatePersistenceServiceInterface:
  val saves = collection.mutable.Buffer[(AggregateId, DeltaContainer[_])]()
  val migrations = collection.mutable.Buffer[AggregateType]()

  def saveAggregate[A : JsonValueCodec](id: AggregateId, aggregate: DeltaContainer[A]): Future[Unit] =
    saves.append((id, aggregate.asInstanceOf[DeltaContainer[_]]))
    aggregates += id -> aggregate
    Future.successful(())

  def loadAggregate[A : JsonValueCodec](id: AggregateId): Future[Option[DeltaContainer[A]]] =
    Future.successful(aggregates.get(id).map(_.asInstanceOf[DeltaContainer[A]]))

  def deleteAggregate[A : JsonValueCodec](id: AggregateId): Unit =
    ()

  def migrationForRepository(aggregateType: AggregateType): Unit =
    migrations.append(aggregateType)

  def boot: Unit =
    ()
