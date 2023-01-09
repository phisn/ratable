package webapp.device.storage

import cats.data.*
import cats.implicits.*
import cats.effect.*
import core.framework.*
import core.messages.common.*
import kofre.base.*
import org.scalajs.dom
import org.scalajs.dom.*
import rescala.default.*
import scalajs.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.*
import webapp.services.*

case class IndexBound()

case class IndexBoundLower(lower: Int)
  
trait StorageDatabaseInterface:
  def put[A <: js.Any](name: String, key: String)(value: A): EitherT[Future, RatableError, Unit]
  def get[A <: js.Any](name: String, key: String): EitherT[Future, RatableError, Option[A]]
  def all[A <: js.Any](name: String, index: String): EitherT[Future, RatableError, Seq[(String, A)]]
  def all[A <: js.Any](name: String, index: String, range: IDBKeyRange): EitherT[Future, RatableError, Seq[(String, A)]]

class StorageDatabase(services: Services, db: Future[IDBDatabase]) extends StorageDatabaseInterface:
  def put[A <: js.Any](name: String, key: String)(value: A) =
    openStoreFor(name, IDBTransactionMode.readwrite) { store =>
      val promise = Promise[Either[RatableError, Unit]]()
      val request = store.put(value, key)

      request.onsuccess = event =>
        services.logger.trace(s"Wrote aggregate with id: $key in $name")
        promise.success(Right(()))

      request.onerror = event =>
        services.logger.error(s"IndexedDB: Transaction failed while putting $key into $name: ${request.error.message}")
        promise.success(Left(RatableError(s"IndexedDB: Transaction failed while putting $key into $name: ${request.error.message}")))

      promise
    }

  def get[A <: js.Any](name: String, key: String) =
    openStoreFor(name, IDBTransactionMode.readonly) { store =>
      val promise = Promise[Either[RatableError, Option[A]]]()
      val request = store.get(key)

      request.onsuccess = event =>
        if js.isUndefined(request.result) then
          promise.success(
            // IndexedDB store get returns undefined if the key is not found
            // https://w3c.github.io/IndexedDB/#dom-idbobjectstore-get
            if js.isUndefined(request.result) then 
              Right(None)
            else 
              Right(Some(request.result.asInstanceOf[A]))
          )
        // }

      request.onerror = event =>
        services.logger.error(s"IndexedDB: Transaction failed while getting $key from $name: ${request.error.message}")
        promise.success(Left(RatableError(s"IndexedDB: Transaction failed while getting $key from $name: ${request.error.message}")))

      promise
    }

  def all[A <: js.Any](name: String, index: String): EitherT[Future, RatableError, Seq[(String, A)]] =
    services.logger.trace(s"IndexedDB: Getting all from $name")

    openStoreFor(name, IDBTransactionMode.readonly) { store =>
      services.logger.trace(s"Processing")

      val promise = Promise[Either[RatableError, Seq[(String, A)]]]()
      val request = store.openCursor()

      val buffer  = collection.mutable.Buffer[(String, A)]()

      request.onsuccess = event =>
        if request.result == null then
          services.logger.trace(s"IndexedDB: Read all from $name from index $index n=${buffer.size}")
          promise.success(Right(buffer.toSeq))
        else
          buffer.append((
            request.result.primaryKey.asInstanceOf[String],
            request.result.value.asInstanceOf[A]
          ))

          request.result.continue()

      request.onerror = event =>
        services.logger.error(s"IndexedDB: Transaction failed while testing $name $index: ${request.error.message}")
        promise.success(Left(RatableError(s"IndexedDB: Transaction failed while testing $name $index: ${request.error.message}")))

      services.logger.trace(s"Processing end")
      promise
    }

  def all[A <: js.Any](name: String, index: String, range: IDBKeyRange): EitherT[Future, RatableError, Seq[(String, A)]] =
    services.logger.trace(s"IndexedDB: Getting all from $name")

    openStoreFor(name, IDBTransactionMode.readonly) { store =>
      services.logger.trace(s"Processing")

      val promise = Promise[Either[RatableError, Seq[(String, A)]]]()
      val request = store.index(index).openCursor(range)

      val buffer  = collection.mutable.Buffer[(String, A)]()

      request.onsuccess = event =>
        if request.result == null then
          services.logger.trace(s"IndexedDB: Read all from $name from index $index n=${buffer.size}")
          promise.success(Right(buffer.toSeq))
        else
          buffer.append((
            request.result.primaryKey.asInstanceOf[String],
            request.result.value.asInstanceOf[A]
          ))

          request.result.continue()

      request.onerror = event =>
        services.logger.error(s"IndexedDB: Transaction failed while testing $name $index $range: ${request.error.message}")
        promise.success(Left(RatableError(s"IndexedDB: Transaction failed while testing $name $index $range: ${request.error.message}")))
      
      services.logger.trace(s"Processing end")
      promise
    }

  private def customDelay(f: => Unit) =
    scala.scalajs.js.timers.setTimeout(2000) {
      f
    }

  private def openStoreFor[R](name: String, mode: IDBTransactionMode)(f: IDBObjectStore => Promise[Either[RatableError, R]]): EitherT[Future, RatableError, R] =
    EitherT(db.flatMap(db =>
      val tx = db.transaction(name, mode)
      val store = tx.objectStore(name)

      tx.oncomplete = event =>
        services.logger.trace(s"IndexedDB: Transaction complete: $name")

      tx.onerror = event =>
        services.logger.error(s"IndexedDB: Transaction failed $name: ${tx.error.message}")

      f(store).future
    ))
