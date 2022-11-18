package webapp.application.pages.viewpage

import core.domain.aggregates.ratable.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.components.*
import webapp.application.components.layouts.*
import webapp.application.pages.ratepage.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def viewRatingsComponent(ratable: Ratable)(using services: Services) =
  val categoriesWithRating = ratable.categoriesWithRating

  if categoriesWithRating.isEmpty then
    div(
      cls := "text-2xl font-bold",
      "There are no ratings for this ratable yet."
    )
  else
    val overallRating = categoriesWithRating
      .map{ case (_, (_, value)) => value }
      .sum / categoriesWithRating.size

    div(
      cls := "flex flex-col space-y-4 items-center md:items-start",
      ratingWithLabelComponent("Overall", Some(overallRating), true),
      div(
        cls := "divider"
      ),
      div(
        cls := "flex flex-col space-y-6 items-center md:items-start",
        categoriesWithRating.toList.sortBy(_._1)
          .map{ case (index, (category, value)) =>
            ratingWithLabelComponent(
              category.title.map(_.value).getOrElse(""),
              Some(value), true
            )
          }
      )
    )
