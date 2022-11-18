package webapp.application.components

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def titleComponent(title: String) =
  div(
    cls := "text-2xl md:text-4xl",
    h1(
      title
    )
  )
