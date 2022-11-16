package webapp.mocks

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.messages.common.*
import core.state.*
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
  val aggregates: collection.mutable.Map[AggregateGid, DeltaContainer[_]] = Map.empty
) extends StatePersistenceServiceInterface:
  val saves = collection.mutable.Buffer[(AggregateGid, DeltaContainer[_])]()
  val migrations = collection.mutable.Buffer[AggregateType]()

  def saveAggregate[A : JsonValueCodec](gid: AggregateGid, aggregate: DeltaContainer[A]): Future[Unit] =
    saves.append((gid, aggregate.asInstanceOf[DeltaContainer[_]]))
    aggregates += gid -> aggregate
    Future.successful(())

  def loadAggregate[A : JsonValueCodec](gid: AggregateGid): Future[Option[DeltaContainer[A]]] =
    Future.successful(aggregates.get(gid).map(_.asInstanceOf[DeltaContainer[A]]))

  def deleteAggregate[A : JsonValueCodec](gid: AggregateGid): Unit =
    ()

  def migrationForRepository(aggregateType: AggregateType): Unit =
    migrations.append(aggregateType)

  def boot: Unit =
    ()
