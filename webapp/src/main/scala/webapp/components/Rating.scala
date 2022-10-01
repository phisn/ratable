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

def rating(id: String, rating: Rating)(using services: Services) =
  div(
    cls := "flex space-x-4",
    div(
      cls := "w-96",
      id
    ),
    div( 
      rating.value match {
        case Some(register) => register.value
        case None => 0
      }
    )
  )
