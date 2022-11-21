package webapp.application.pages.viewpage

import core.domain.aggregates.ratable.*
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

def badgesComponent(ratable: Ratable)(using services: Services) =
  div(
    cls := "flex space-x-2 md:space-x-4",

    badgeComponent(s"${ratable._ratings.size} Submissions"),
    badgeComponent(s"0 Comments")(
    ),
    badgeComponent("7 days")
  )
