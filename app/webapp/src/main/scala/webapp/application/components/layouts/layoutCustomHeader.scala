package webapp.application.components.layouts

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def layoutCustomHeaderComponent(header: VNode)(body: VNode)(using services: Services) =
  div(
    cls := "flex flex-col min-h-screen bg-base-100",
    header,
    div(
      cls := "flex-grow flex flex-col",
      body
    ),
    footerComponent
  )
