package webapp.application.pages.ratepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.components.*
import webapp.application.components.common.*
import webapp.application.components.layouts.*
import webapp.application.pages.homepage.*
import webapp.application.pages.viewpage.*
import webapp.services.*
import webapp.state.framework.{given, *}
import webapp.application.usecases.ratable.*
import webapp.{given, *}

case class RatePage(
  ratableID: String
) extends Page:
  def render(using services: Services): VNode =
    val ratingForCategorySignal = Var(Map[Int, Int]())

    layoutSingleRatable(ratableID)(ratable =>
      contentHorizontalCenterComponent(
        titleComponent(ratable.title),
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
