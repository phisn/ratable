package webapp.pages.ratepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.*
import webapp.components.layouts.*
import webapp.pages.viewpage.*
import webapp.services.*
import webapp.store.aggregates.rating.{given, *}
import webapp.store.framework.*
import webapp.{*, given}

case class RatePage(
  ratableId: String
) extends Page:
  def render(using services: Services): HtmlVNode =
    layoutComponent(
      div(
        cls := "flex-grow flex justify-center p-4 mt:p-12",
        div(
          cls := "flex flex-col space-y-8",
          width := "36rem",
          
          titleComponent("Rating of this cool chinese restaurant we went to"),
          
          div(
            cls := "flex flex-col space-y-6 items-center md:items-start",
            ratingWithLabelComponent("Taste"),
            ratingWithLabelComponent("Ambiente"),
            ratingWithLabelComponent("Price"),
          ),

          div(
            cls := "flex flex-col md:flex-row pt-4 space-y-4 md:space-y-0 md:space-x-4",
            button(
              cls := "btn btn-primary",
              "Submit",
              onClick.foreach(_ => services.routing.to(ViewPage(ratableId)))
            ),
            button(
              cls := "btn btn-outline",
              "Cancel and view submissions",
              onClick.foreach(_ => services.routing.to(ViewPage(ratableId)))
            )
          )
        )
      )
    )
