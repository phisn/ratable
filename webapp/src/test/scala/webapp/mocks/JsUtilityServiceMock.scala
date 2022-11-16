package webapp.mocks

import rescala.default.*
import webapp.services.*

class JsUtilityServiceMock extends JsUtilityServiceInterface:
  def windowEventAsEvent[T](eventName: String) =
    Evt[T]()
  
