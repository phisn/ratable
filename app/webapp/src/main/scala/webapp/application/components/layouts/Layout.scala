package webapp.application.components.layouts

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.*
import webapp.application.components.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def layoutComponent(body: VNode)(using services: ServicesWithApplication) =
  layoutCustomHeaderComponent(headerComponent)(body)
