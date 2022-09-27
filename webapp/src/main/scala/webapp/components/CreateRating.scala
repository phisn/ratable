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
  val newestID = Var[Long](0)

  clickEvent.observe(ratingValue =>
    newestID.set(ratingsNew(ratingValue))
  )

  div(
    display := "flex",
    button(onClick.map(_ => Random.between(0, 10)) --> clickEvent),
    div(
      marginLeft := "15px",
      newestID
    )
  )
