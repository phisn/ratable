package webapp.application.components

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def contentFullCenterComponent =
  div(
    cls := "flex-grow flex flex-col justify-center items-center p-4"
  )
