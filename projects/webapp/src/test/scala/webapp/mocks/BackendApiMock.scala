package webapp.mocks

import rescala.default.*
import webapp.services.*

class BackendApiMock extends BackendApiInterface:
  def hello(username: String): Signal[Option[String]] =
    Signal(Some(s"Hello $username"))
