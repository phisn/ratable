package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import scalajs.*
import org.scalajs.dom
import org.scalajs.dom.*
import rescala.default.*
import scala.concurrent.*
import webapp.services.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import core.state.framework.*
import webapp.state.framework.{given, *}

trait StatePersistenceServiceInterface:
  def saveAggregate[A : JsonValueCodec](aggregateTypeId: String, id: String, aggregate: DeltaContainer[A]): Unit
  def loadAggregate[A : JsonValueCodec](aggregateTypeId: String, id: String): Future[Option[DeltaContainer[A]]]
  def deleteAggregate[A : JsonValueCodec](aggregateTypeId: String, id: String): Unit

  def migrationForRepository(aggregateTypeId: String): Unit
  def boot: Unit

class StatePersistenceService(services: {
  val logger: LoggerServiceInterface
}) extends StatePersistenceServiceInterface:
  def saveAggregate[A : JsonValueCodec](aggregateTypeId: String, id: String, aggregate: DeltaContainer[A]) =
    ()

  def loadAggregate[A : JsonValueCodec](aggregateTypeId: String, id: String): Future[Option[DeltaContainer[A]]] =
    openStoreFor(aggregateTypeId) { store =>
      val promise = Promise[Option[DeltaContainer[A]]]()
      val request = store.get(id)

      request.onsuccess = event =>
        promise.success(
          // IndexedDB store get returns undefined if the key is not found
          // https://w3c.github.io/IndexedDB/#dom-idbobjectstore-get
          if js.isUndefined(request.result) then 
            None
          else 
            Some(readFromString[DeltaContainer[A]](request.result.asInstanceOf[JsAggregateContainer].aggregateJson))
        )

      request.onerror = event =>
        services.logger.error(s"IndexedDB: Transaction failed while getting $id: ${request.error.message}")
        promise.failure(new Exception(s"IndexedDB: Transaction failed while getting $id: ${request.error.message}"))

      promise
    }

  def deleteAggregate[A : JsonValueCodec](aggregateTypeId: String, id: String) =
    ()
  
  private def openStoreFor[R](id: String)(f: IDBObjectStore => Promise[R]): Future[R] =
    db.flatMap(db =>
      val tx = db.transaction(id, IDBTransactionMode.readonly)
      val store = tx.objectStore(id)

      tx.oncomplete = event =>
        services.logger.trace(s"IndexedDB: Transaction complete: $id")

      tx.onerror = event =>
        services.logger.error(s"IndexedDB: Transaction failed $id: ${tx.error.message}")

      f(store).future
    )

  private def newMigration(version: Int)(migration: IDBDatabase => Unit): Unit =
    migrations.getOrElseUpdate(version, collection.mutable.Set()) += migration

  def migrationForRepository(aggregateTypeId: String) =
    newMigration(1) { db =>
      val store = db.createObjectStore(aggregateTypeId)
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
            .foreach(_(event.target.result))
          services.logger.trace("IndexedDB upgrade done")

        request.onsuccess = event =>
          services.logger.log("IndexedDB opened successfully")
          dbPromise.success(event.target.result)
        
        request.onerror = event =>
          services.logger.error(s"IndexedDB open failed with error: ${event.message}")
          dbPromise.failure(new Exception(s"IndexedDB open failed with error: ${event.message}"))

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
