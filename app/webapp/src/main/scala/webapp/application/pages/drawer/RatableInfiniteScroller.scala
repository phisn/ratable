package webapp.application.pages.drawer

import core.domain.aggregates.ratable.ecmrdt.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.*
import webapp.{given, *}
import webapp.application.*
import webapp.application.components.common.*
import webapp.application.components.icons.*
import webapp.application.pages.viewpage.*
import webapp.application.framework.given
import webapp.application.services.*
import webapp.services.*
import webapp.state.framework.*

def ratableInfiniteScrollerComponent(using services: ServicesWithApplication): VNode =
  infiniteScrollerComponent(() => 
    services.state.ratables.all.map(ratables =>
      Retrievals(
        ratables.map((id, ratable) =>
          ratableEntryComponent(id.aggregateId, ratable)
        ),
        false
      )
    ).andThen {
      case Success(_) =>
        println("Successhadsfklu ahsdkfh aweflkjaweh flkasdjf hasldkj ")
      case Failure(exception) =>
        println(s"Failure: $exception")
    }
  )

def ratableEntryComponent(id: String, ratable: Ratable)(using services: ServicesWithApplication) =
  val categoriesWithRating = ratable.categoriesWithRating

  val overallRating = 
    if categoriesWithRating.isEmpty then
      0
    else
      categoriesWithRating
        .map{ case (_, (_, value)) => value }
      .sum / categoriesWithRating.size

  div(
    cls := "flex flex-col items-center w-full",
    div(
      cls := "flex items-center space-x-4",
      a(
        cls := "flex flex-col transition hover:bg-base-300 rounded-xl p-4 space-y-2",
        div(
          cls := "text-xl",
          // with random string added
          ratable.title
        ),
        div(
          cls := "flex items-center w-full",
          ratingComponent(overallRating)
        ),
        div(
          cls := "py-2",
          div(
            cls := "badge badge-outline",
            services.local.get("page.drawer.badge.submissions").map(label =>
              s"${ratable.ratings.size} $label"
            )
          ),
        ),

        href := services.routing.link(ViewPage(id)),
        onClick.preventDefault.foreach(_ =>
          services.routing.to(ViewPage(id))
        )
      ),
      button(
        cls := "btn btn-ghost btn-square hover:btn-warning",
        iconTrash(
          cls := "w-8 h-8"
        )
      )
    )
  )
