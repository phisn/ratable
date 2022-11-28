package webapp.application.pages.debugpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.util.*
import webapp.*
import webapp.application.*
import webapp.application.framework.{given, *}
import webapp.services.*
import webapp.state.framework.*

def createRating(using ServicesWithApplication) = 
  val clickEvent = Evt[Int]()

  div(
    cls := "flex space-x-4",
    button(
      cls := "btn btn-primary",
      "Create Rating",
      onClick.map(_ => Random.between(0, 10)) --> clickEvent
    )
  )
