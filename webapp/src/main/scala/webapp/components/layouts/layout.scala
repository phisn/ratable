package webapp.components.layouts

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def layoutComponent(body: VNode)(using services: Services) =
  layoutCustomHeaderComponent(headerComponent)(body)
