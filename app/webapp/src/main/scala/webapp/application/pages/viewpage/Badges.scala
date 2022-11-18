package webapp.application.pages.viewpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.components.*
import webapp.application.components.layouts.*
import webapp.application.pages.ratepage.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def badgesComponent(using services: Services) =
  div(
    cls := "flex space-x-2 md:space-x-4",

    badgeComponent("36 Submissions"),
    badgeComponent("20 Comments")(
    ),
    badgeComponent("2 days")
  )
