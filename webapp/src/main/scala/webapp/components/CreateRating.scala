package webapp.components

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.store.aggregates.ratings.given
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.Services
import webapp.given
import webapp.usecases.ratings.*

import scala.util.*

def createRating(using Services) = 
  val clickEvent = Evt[Int]()

  div(
    cls := "flex space-x-4",
    button(
      cls := "btn btn-primary",
      "Create Rating",
      onClick.map(_ => Random.between(0, 10)) --> clickEvent
    ),
    div(clickEvent.map(ratingsNew).latest(0L))
  )
