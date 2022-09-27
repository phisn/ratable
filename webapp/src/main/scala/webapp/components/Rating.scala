package webapp.components

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.Services
import webapp.given

def rating(id: Int, rating: Rating)(using services: Services) =
  div(
    display := "flex",
    div(
      width := "200px",
      id & 0x00000000ffffffffL
    ),
    div(
      marginLeft := "5px",
      rating.value match {
        case Some(register) => register.value
        case None => 0
      }
    )
  )
