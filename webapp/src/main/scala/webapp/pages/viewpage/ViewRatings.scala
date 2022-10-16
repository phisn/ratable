package webapp.pages.viewpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.*
import webapp.components.layouts.*
import webapp.pages.ratepage.*
import webapp.services.*
import webapp.store.framework.*
import webapp.{*, given}

def viewRatingsComponent(using services: Services) =
  div(
    cls := "flex flex-col space-y-6 items-center md:items-start",
    div(
      cls := "flex flex-col space-y-4 items-center md:items-start",
      ratingWithLabelComponent("Overall", None, true),
      div(
        cls := "divider"
      ),
      ratingWithLabelComponent("Taste", None, true),
    ),
    ratingWithLabelComponent("Ambiente", None, true),
    ratingWithLabelComponent("Price", None, true),
  )
