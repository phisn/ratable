package webapp.application.pages.viewpage

import core.domain.aggregates.ratable.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.*
import webapp.application.components.*
import webapp.application.components.layouts.*
import webapp.application.framework.given
import webapp.application.pages.ratepage.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def badgesComponent(ratable: Ratable)(using services: ServicesWithApplication) =
  div(
    cls := "flex space-x-2 md:space-x-4",

    services.local.get(
      if ratable._ratings.size == 1 then
        "page.view.badge.submissions.singular"
      else
        "page.view.badge.submissions"
    ).map(label =>
      badgeComponent(s"${ratable._ratings.size} $label")
    ),
    services.local.get(
      if 0 == 1 then
        "page.view.badge.comments.singular"
      else
        "page.view.badge.comments"
    ).map(label =>
      badgeComponent(s"0 $label")
    ),
    services.local.get("page.view.badge.days").map(label =>
      badgeComponent(s"7 $label")
    ),
  )
