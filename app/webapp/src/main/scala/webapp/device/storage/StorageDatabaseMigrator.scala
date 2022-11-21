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

trait StorageDatabaseMigratorInterface:
  def store(name: String, indexes: Set[String] = Set.empty): StorageDatabaseMigratorInterface
  def remove(name: String): StorageDatabaseMigratorInterface

class StorageDatabaseMigrator(services: Services, db: IDBDatabase) extends StorageDatabaseMigratorInterface:
  def store(name: String, indexes: Set[String] = Set.empty): StorageDatabaseMigratorInterface = 
    val store = db.createObjectStore(name)

    indexes.foreach(index => 
      store.createIndex(index, index)
    )

    this

  def remove(name: String): StorageDatabaseMigratorInterface = 
    if db.objectStoreNames.contains(name) then
      db.deleteObjectStore(name)

    this
