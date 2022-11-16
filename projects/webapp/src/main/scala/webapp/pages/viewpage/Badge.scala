package webapp.pages.viewpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.*
import webapp.components.layouts.*
import webapp.pages.ratepage.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def badgeComponent(using services: Services) =
  div(
    cls := "badge badge-outline p-3 md:p-4",
  )
