package webapp.device.services

import webapp.*
import webapp.device.storage.*

trait StorageServiceInterface:
  def openDatabase(name: String, version: Int = 1): StorageDatabaseBuilderInterface

class StorageService(services: Services) extends StorageServiceInterface:
  def openDatabase(name: String, version: Int = 1) = StorageDatabaseBuilder(name, version, services)    
