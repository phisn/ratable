package webapp.components.layouts

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.store.framework.*
import webapp.{*, given}

def layoutComponent(body: HtmlVNode)(using services: Services) =
  layoutCustomHeaderComponent(headerComponent)(body)
