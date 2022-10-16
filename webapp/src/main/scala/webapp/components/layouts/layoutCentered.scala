package webapp.components.layouts

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.*
import webapp.services.*
import webapp.store.aggregates.rating.{given, *}
import webapp.store.framework.*
import webapp.{*, given}

def layoutCenteredComponent(body: HtmlVNode)(using services: Services) =
  layoutComponent(
    centerContentComponent(body)
  )
