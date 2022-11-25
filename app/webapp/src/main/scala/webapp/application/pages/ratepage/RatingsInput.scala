package webapp.application.pages.ratepage

import core.domain.aggregates.ratable.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.components.*
import webapp.application.components.common.*
import webapp.application.components.layouts.*
import webapp.application.framework.*
import webapp.application.pages.viewpage.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def ratingsInputComponent(ratable: Ratable, ratingForCategorySignal: PromiseSignal[Map[Int, Int]]) =
  val ratings = ratable.categories.map(_ => PromiseSignal[Int]()).toSeq

  ratingForCategorySignal :=
    ratings
      .map(_.value)
      .zipWithIndex
      .map((a, b) => (b, a))
      .toMap

  div(
    cls := "flex flex-col space-y-6 items-center md:items-start",
    ratable.categories.toList.sortBy(_._1)
      .map((index, category) =>
        ratingWithLabelInputComponent(
          category.title.map(_.value).getOrElse(""),
          ratings(index)
        )
      )
  )
