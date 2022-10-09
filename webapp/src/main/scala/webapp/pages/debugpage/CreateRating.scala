package webapp.pages.debugpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.util.*
import webapp.{given, *}
import webapp.services.*
import webapp.store.aggregates.ratings.given
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.usecases.ratings.*

def createRating(using Services) = 
  val clickEvent = Evt[Int]()

  div(
    cls := "flex space-x-4",
    button(
      cls := "btn btn-primary",
      "Create Rating",
      onClick.map(_ => Random.between(0, 10)) --> clickEvent
    ),
    div(clickEvent.map(ratingsNew).latest(""))
  )
