package webapp.application.pages.ratepage

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.*
import webapp.application.*
import webapp.application.components.*
import webapp.application.components.common.*
import webapp.application.components.layouts.*
import webapp.application.framework.*
import webapp.application.framework.given
import webapp.application.pages.homepage.*
import webapp.application.pages.viewpage.*
import webapp.application.services.*
import webapp.application.usecases.ratable.*
import webapp.device.framework.given
import webapp.services.*
import webapp.state.framework.{given, *}
import webapp.{given, *}

case class RatePage(
  ratableId: String
) extends Page:
  val aggregateId: AggregateId = readFromString(ratableId)

  def render(using services: ServicesWithApplication): VNode =
    val ratingForCategorySignal = PromiseSignal(Map[Int, Int]())

    layoutSingleRatable(aggregateId)(ratable =>
      contentHorizontalCenterComponent(
        titleComponent(ratable.title),
        ratingsInputComponent(ratable, ratingForCategorySignal),

        div(
          cls := "flex flex-col md:flex-row pt-4 space-y-4 md:space-y-0 md:space-x-4",
          button(
            cls := "btn btn-outline",
            services.local.get("page.rate.cancelButton"),
            onClick.foreach(_ => services.routing.toReplace(ViewPage(ratableId)))
          ),
          button(
            cls := "btn btn-primary",
            services.local.get("page.rate.submitButton"),
            onClick.foreach(_ => {
              rateRatable(aggregateId, "fd3t8TjWKf7SpIz7cg", ratingForCategorySignal.now).value.andThen {
                case Success(Some(message)) =>
                  services.logger.error(s"Rate error messge '${message}'")

                case Failure(exception) =>
                  services.logger.error(s"Rate failed ${exception}")
                
                case _ =>
                  services.logger.log(s"Rate succeeded")
              }
              services.routing.toReplace(ViewPage(ratableId))
            })
          ),
        )
      )
    )
