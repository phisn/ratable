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

trait StorageDatabaseBuilderInterface:
  def newMigration(db: StorageDatabaseMigratorInterface => Unit)
    : StorageDatabaseBuilderInterface =
    newMigration(1)(db)

  def newMigration(version: Int)(db: StorageDatabaseMigratorInterface => Unit)
    : StorageDatabaseBuilderInterface
    
  // Database can be used before it is build. Idea is to
  // assume itll be built in the future
  def assume: StorageDatabaseInterface

  def build: StorageDatabaseInterface

class StorageDatabaseBuilder(name: String, version: Int, services: Services) extends StorageDatabaseBuilderInterface:
  private val migrations = collection.mutable.Map[Int, collection.mutable.Set[StorageDatabaseMigratorInterface => Unit]]()
  private val promise = Promise[IDBDatabase]()
  private val db = StorageDatabase(services, promise.future)
  
  def newMigration(version: Int)(db: StorageDatabaseMigratorInterface => Unit)
    : StorageDatabaseBuilderInterface = 
    migrations.getOrElseUpdate(version, collection.mutable.Set()) += db
    this

  def assume: StorageDatabaseInterface =
    db

  def build: StorageDatabaseInterface =
    // Dont build if already built
    if !promise.isCompleted then
      dom.window.indexedDB.toOption match
        case Some(indexedDB) =>
          val request = indexedDB.`open`(name, version)

          request.onupgradeneeded = event =>
            services.logger.trace("IndexedDB upgrade needed")
            
            migrations
              .filter(_(0) >= event.oldVersion)
              .values
              .flatten
              .foreach(_(StorageDatabaseMigrator(services, request.result)))

            services.logger.trace("IndexedDB upgrade done")

          request.onsuccess = _ =>
            services.logger.log("IndexedDB opened successfully")
            promise.success(request.result)
          
          request.onerror = _ =>
            services.logger.error(s"IndexedDB open failed with error: (${request.error})")
            promise.failure(new Exception(s"IndexedDB open failed with error: (${request.error})"))

        case None => 
          services.logger.error("IndexedDB not supported")
          promise.failure(new Exception("IndexedDB not supported"))
      
    db
