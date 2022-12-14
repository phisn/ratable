package webapp.application.pages.debugpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.application.framework.{given, *}

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