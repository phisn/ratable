package webapp.mocks

import rescala.default.*
import webapp.services.*

class BackendApiServiceMock extends BackendApiServiceInterface:
  def hello(username: String): Signal[Option[String]] =
    Signal(Some(s"Hello $username"))
