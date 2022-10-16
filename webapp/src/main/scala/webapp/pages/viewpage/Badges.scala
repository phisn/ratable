package webapp.pages.viewpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.*
import webapp.components.layouts.*
import webapp.pages.ratepage.*
import webapp.services.*
import webapp.store.aggregates.rating.{given, *}
import webapp.store.framework.*
import webapp.{*, given}

def badgesComponent(using services: Services) =
  div(
    cls := "flex space-x-2 md:space-x-4",

    badgeComponent("36 Submissions"),
    badgeComponent("20 Comments")(
    ),
    badgeComponent("2 days")
  )
