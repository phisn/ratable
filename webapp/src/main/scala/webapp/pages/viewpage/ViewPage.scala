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
          width := "36rem",
          
          titleComponent("Rating of this cool chinese restaurant we went to"),

          div(
            cls := "flex flex-col md:flex-row space-y-2 md:space-y-0 md:space-x-4",
            div(
              cls := "badge badge-outline w-full md:w-auto p-4",
              "36 Submissions"
            ),
            div(
              cls := "badge badge-outline w-full md:w-auto p-4",
              "20 Comments"
            ),
            div(
              cls := "badge badge-outline w-full md:w-auto p-4",
              "Submission ends in 2 days"
            )
          ),

          div(
            cls := "pt-6",
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
          )
        )
      )
    )
