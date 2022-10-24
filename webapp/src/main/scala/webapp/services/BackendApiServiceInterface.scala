package webapp.services

import rescala.default.*

trait BackendApiServiceInterface:
  def hello(username: String): Signal[Option[String]]
