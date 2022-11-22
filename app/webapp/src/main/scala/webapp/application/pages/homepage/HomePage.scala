package webapp.application.pages.homepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.components.*
import webapp.application.components.layouts.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

case class HomePage() extends Page:
  def render(using services: Services): VNode =
    layoutCustomHeaderComponent(
      headerCleanComponent
    )(
      contentFullCenterComponent(
        div(
          cls := "space-y-8 md:space-y-16",
          h1(
            cls := "text-5xl font-bold text-center",
            "Create your own Ratable"
          ),
          ratableInputComponent
        )
      )
    )
