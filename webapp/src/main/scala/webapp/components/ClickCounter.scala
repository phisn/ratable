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
    cls := "indicator",
    span(
      cls := "indicator-item badge badge-accent", 
      counter
    ),
    button(
      cls := "btn btn-primary rounded-none",
      "Click me now",
      onClick(counter.map(_ + 1)) --> counter
    )
  )