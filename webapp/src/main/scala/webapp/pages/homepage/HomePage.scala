package webapp.pages.homepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.*
import webapp.components.layouts.*
import webapp.services.*
import webapp.store.aggregates.rating.{*, given}
import webapp.store.framework.*
import webapp.{*, given}

case class HomePage() extends Page:
  def render(using services: Services): HtmlVNode =
    layoutCustomHeaderComponent(
      headerCleanComponent
    )(
      centerContentComponent(
        div(
          div(
            cls := "space-y-8 md:space-y-16 p-4",
            h1(
              cls := "text-5xl font-bold text-center",
              "Create your own Ratable"
            ),
            ratableInputComponent
          )
        )
      )
    )
