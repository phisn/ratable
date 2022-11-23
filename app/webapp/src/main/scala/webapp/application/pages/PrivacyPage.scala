package webapp.application.pages

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.components.*
import webapp.application.components.common.*
import webapp.application.components.layouts.*
import webapp.application.pages.homepage.*
import webapp.application.pages.ratepage.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

case class PrivacyPage() extends Page:
  def render(using services: Services) =
    layoutComponent(
      contentHorizontalCenterComponent(
        div(
        )
      )
    )
