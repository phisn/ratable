package webapp.pages.ratepage

import core.store.aggregates.ratable.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.*
import webapp.components.layouts.*
import webapp.pages.viewpage.*
import webapp.services.*
import webapp.store.framework.*
import webapp.{*, given}

def ratingsInputComponent(ratable: Ratable, ratingForCategorySignal: Var[Map[Int, Int]]) =
  div(
    cls := "flex flex-col space-y-6 items-center md:items-start",
    ratable.categories.toList.sortBy(_._1)
      .map((index, category) =>
        val ratingSignal = Var(3)

        ratingSignal.observe { rating =>
          ratingForCategorySignal.set(ratingForCategorySignal.now.updated(index, rating))
        }

        ratingWithLabelComponent(
          category.title.map(_.value).getOrElse(""),
          None, false, 5,
          ratingSignal
        )
      )
  )
