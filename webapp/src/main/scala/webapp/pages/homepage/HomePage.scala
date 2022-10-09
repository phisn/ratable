package webapp.pages.homepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.*
import webapp.services.*
import webapp.store.aggregates.ratings.given
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.{*, given}

case class HomePage() extends Page:
  def render(using services: Services): HtmlVNode =
    centerPage(
      div(
        cls := "space-y-8",
        h1(
          cls := "text-5xl font-bold",
          "Create your own Ratable"
        ),
        ratableInput
      )
    )
    /*
    div(
      cls := "hero min-h-screen bg-base-200",
      div(
        cls := "hero-content text-center",
        div(
          cls := "space-y-8",
          h1(
            cls := "text-5xl font-bold",
            "Create your own Ratable"
          ),
          ratableInput
        )
      )
    )
    */


  