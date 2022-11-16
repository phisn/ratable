package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.messages.common.*
import kofre.base.*
import scalajs.*
import org.scalajs.dom
import org.scalajs.dom.*
import rescala.default.*
import scala.concurrent.*
import webapp.services.*

import core.state.*
import core.state.framework.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import webapp.state.framework.{given, *}

// https://www.w3.org/TR/IndexedDB/
trait StatePersistenceServiceInterface:
  def saveAggregate[A : JsonValueCodec](gid: AggregateGid, aggregate: DeltaContainer[A]): Future[Unit]
  def loadAggregate[A : JsonValueCodec](gid: AggregateGid): Future[Option[DeltaContainer[A]]]
  def deleteAggregate[A : JsonValueCodec](gid: AggregateGid): Unit

  def migrationForRepository(aggregateType: AggregateType): Unit
  def boot: Unit

class StatePersistenceService(services: {
  val logger: LoggerServiceInterface
}) extends StatePersistenceServiceInterface:
  def saveAggregate[A : JsonValueCodec](gid: AggregateGid, aggregate: DeltaContainer[A]) =
    openStoreFor(gid.aggregateType, IDBTransactionMode.readwrite) { store =>
      services.logger.trace(s"Saving aggregate with id: ${gid}")
      val promise = Promise[Unit]()
      val request = store.put(JsAggregateContainer(writeToString(aggregate), aggregate.maxTag), gid.aggregateId)

      request.onsuccess = event =>
        services.logger.trace(s"Saved aggregate with id: ${gid}")
        promise.success(())

      request.onerror = event =>
        services.logger.error(s"IndexedDB: Transaction failed while getting ${gid}: ${request.error.message}")
        promise.failure(Exception(s"IndexedDB: Transaction failed while getting ${gid}: ${request.error.message}"))

      promise
    }

  def loadAggregate[A : JsonValueCodec](gid: AggregateGid): Future[Option[DeltaContainer[A]]] =
    openStoreFor(gid.aggregateType, IDBTransactionMode.readonly) { store =>
      val promise = Promise[Option[DeltaContainer[A]]]()
      val request = store.get(gid.aggregateId)

      request.onsuccess = event =>
        scala.scalajs.js.timers.setTimeout(4000) {
          promise.success(
            // IndexedDB store get returns undefined if the key is not found
            // https://w3c.github.io/IndexedDB/#dom-idbobjectstore-get
            if js.isUndefined(request.result) then 
              None
            else 
              Some(readFromString[DeltaContainer[A]](request.result.asInstanceOf[JsAggregateContainer].aggregateJson))
          )
        }

      request.onerror = event =>
        services.logger.error(s"IndexedDB: Transaction failed while getting ${gid}: ${request.error.message}")
        promise.failure(Exception(s"IndexedDB: Transaction failed while getting ${gid}: ${request.error.message}"))

      promise
    }

  def deleteAggregate[A : JsonValueCodec](gid: AggregateGid) =
    ()
  
  private def openStoreFor[R](aggregateType: AggregateType, mode: IDBTransactionMode)(f: IDBObjectStore => Promise[R]): Future[R] =
    db.flatMap(db =>
      val tx = db.transaction(aggregateType.name, mode)
      val store = tx.objectStore(aggregateType.name)

      tx.oncomplete = event =>
        services.logger.trace(s"IndexedDB: Transaction complete: $aggregateType")

      tx.onerror = event =>
        services.logger.error(s"IndexedDB: Transaction failed $aggregateType: ${tx.error.message}")

      f(store).future
    )

  private def newMigration(version: Int)(migration: IDBDatabase => Unit): Unit =
    migrations.getOrElseUpdate(version, collection.mutable.Set()) += migration

  def migrationForRepository(aggregateType: AggregateType) =
    newMigration(1) { db =>
      val store = db.createObjectStore(aggregateType.name)
      store.createIndex("tag", "tag")
    }

  def boot =
    dom.window.indexedDB.toOption match
      case Some(indexedDB) =>
        val request = indexedDB.`open`("aggregates", 1)

        request.onupgradeneeded = event =>
          services.logger.trace("IndexedDB upgrade needed")
          migrations
            .filter(_(0) >= event.oldVersion)
            .values
            .flatten
            .foreach(_(request.result))
          services.logger.trace("IndexedDB upgrade done")

        request.onsuccess = _ =>
          services.logger.log("IndexedDB opened successfully")
          dbPromise.success(request.result)
        
        request.onerror = _ =>
          services.logger.error(s"IndexedDB open failed with error: (${request.error})")
          dbPromise.failure(new Exception(s"IndexedDB open failed with error: (${request.error})"))

      case None => 
        services.logger.error("IndexedDB not supported")
        dbPromise.failure(new Exception("IndexedDB not supported"))

  private val dbPromise = Promise[IDBDatabase]()
  private val db = dbPromise.future

  private val migrations = collection.mutable.Map[Int, collection.mutable.Set[IDBDatabase => Unit]]()
  
  class JsAggregateContainer(
    val aggregateJson: String,
    val tag: Tag

  ) extends scalajs.js.Object
