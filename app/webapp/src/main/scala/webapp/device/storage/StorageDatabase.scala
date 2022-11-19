package webapp.device.storage

import scala.concurrent.ExecutionContext.Implicits.global
import core.messages.common.*
import kofre.base.*
import scalajs.*
import org.scalajs.dom
import org.scalajs.dom.*
import rescala.default.*
import scala.concurrent.*
import webapp.*
import webapp.services.*

trait StorageDatabaseInterface:
  def get[A <: js.Any](name: String, key: String): Future[Option[A]]
  def put[A <: js.Any](name: String, key: String)(value: A): Future[Unit]

class StorageDatabase(services: Services, db: Future[IDBDatabase]) extends StorageDatabaseInterface:
  def get[A <: js.Any](name: String, key: String) =
    openStoreFor(name, IDBTransactionMode.readonly) { store =>
      val promise = Promise[Option[A]]()
      val request = store.get(key)

      request.onsuccess = event =>
        customDelay {
          promise.success(
            // IndexedDB store get returns undefined if the key is not found
            // https://w3c.github.io/IndexedDB/#dom-idbobjectstore-get
            if js.isUndefined(request.result) then 
              None
            else 
              Some(request.result.asInstanceOf[A])
          )
        }

      request.onerror = event =>
        services.logger.error(s"IndexedDB: Transaction failed while getting $key from $name: ${request.error.message}")
        promise.failure(Exception(s"IndexedDB: Transaction failed while getting $key from $name: ${request.error.message}"))

      promise
    }

  def put[A <: js.Any](name: String, key: String)(value: A) =
    openStoreFor(name, IDBTransactionMode.readwrite) { store =>
      val promise = Promise[Unit]()
      val request = store.put(value, key)

      request.onsuccess = event =>
        services.logger.trace(s"Wrote aggregate with id: $key in $name")
        promise.success(())

      request.onerror = event =>
        services.logger.error(s"IndexedDB: Transaction failed while putting $key into $name: ${request.error.message}")
        promise.failure(Exception(s"IndexedDB: Transaction failed while putting $key into $name: ${request.error.message}"))

      promise
    }

  private def customDelay(f: => Unit) =
    scala.scalajs.js.timers.setTimeout(2000) {
      f
    }

  private def openStoreFor[R](name: String, mode: IDBTransactionMode)(f: IDBObjectStore => Promise[R]): Future[R] =
    db.flatMap(db =>
      val tx = db.transaction(name, mode)
      val store = tx.objectStore(name)

      tx.oncomplete = event =>
        services.logger.trace(s"IndexedDB: Transaction complete: $name")

      tx.onerror = event =>
        services.logger.error(s"IndexedDB: Transaction failed $name: ${tx.error.message}")

      f(store).future
    )
