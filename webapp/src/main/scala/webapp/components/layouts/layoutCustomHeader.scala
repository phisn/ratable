package webapp.components.layouts

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.store.framework.*
import webapp.{*, given}

def layoutCustomHeaderComponent(header: HtmlVNode)(body: HtmlVNode)(using services: Services) =
  div(
    cls := "flex flex-col min-h-screen bg-base-100",
    header,
    div(
      cls := "flex-grow flex flex-col",
      body
    ),
    footerComponent
  )
