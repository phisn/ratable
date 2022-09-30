package webapp.components

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.given

import scala.util.*

def clickCounter =
  val counter = Var(0)

  div(
    display := "flex",
    button(
      cls := "btn btn-primary",
      "Click me now",
      onClick(counter.map(_ + 1)) --> counter
    ),
    div(
      marginLeft := "15px",
      "You clicked ", counter, " times"
    ),
  )