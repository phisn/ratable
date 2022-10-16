package webapp.pages.ratepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.*
import webapp.components.layouts.*
import webapp.pages.homepage.*
import webapp.pages.viewpage.*
import webapp.services.*
import webapp.store.framework.{given, *}
import webapp.usecases.ratable.*
import webapp.{given, *}

case class RatePage(
  ratableID: String
) extends Page:
  def render(using services: Services): HtmlVNode =
    val ratingForCategorySignal = Var(Map[Int, Int]())

    layoutSingleRatable(ratableID)(ratable =>
      div(
        cls := "flex-grow flex justify-center p-4 md:p-12",
        div(
          cls := "flex flex-col space-y-8 w-[40rem]",

          titleComponent(ratable.title.map(_.value).getOrElse("")),
          ratingsInputComponent(ratable, ratingForCategorySignal),

          div(
            cls := "flex flex-col md:flex-row pt-4 space-y-4 md:space-y-0 md:space-x-4",
            button(
              cls := "btn btn-primary",
              "Submit",
              onClick.foreach(_ => {
                rateRatable(ratableID, ratingForCategorySignal.now)
                services.routing.toReplace(ViewPage(ratableID))
              })
            ),
            button(
              cls := "btn btn-outline",
              "Cancel and view submissions",
              onClick.foreach(_ => services.routing.toReplace(ViewPage(ratableID)))
            )
          )
        )
      )
    )
