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

case class ViewPage(
  ratableId: String
) extends Page:
  def render(using services: Services): HtmlVNode =
    layoutComponent(
      div(
        cls := "flex-grow flex justify-center p-4 md:p-12",
        div(
          cls := "flex flex-col space-y-6",
          width := "40rem",
          
          titleComponent("Rating of this cool chinese restaurant we went to"),
          badgesComponent,
          viewRatingsComponent(
            cls := "md:pt-6"
          )
        )
      )
    )
