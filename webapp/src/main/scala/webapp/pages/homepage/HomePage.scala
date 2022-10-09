package webapp.pages.homepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.*
import webapp.components.layouts.*
import webapp.services.*
import webapp.store.aggregates.ratings.{*, given}
import webapp.store.framework.*
import webapp.{*, given}

case class HomePage() extends Page:
  def render(using services: Services): HtmlVNode =
    layoutCustomHeader(
      headerCleanComponent
    )(
      centerContent(
        div(
          cls := "space-y-16",
          h1(
            cls := "text-5xl font-bold text-center",
            "Create your own Ratable"
          ),
          ratableInput
        )
      )
    )
