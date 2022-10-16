package webapp.pages.ratepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.*
import webapp.components.layouts.*
import webapp.pages.viewpage.*
import webapp.services.*
import webapp.store.aggregates.ratable.*
import webapp.store.framework.*
import webapp.{*, given}

def ratingsInputComponent(ratable: Ratable) =
  div(
    cls := "flex flex-col space-y-6 items-center md:items-start",
    ratable.categories.toList.sortBy(_._1)
      .map((index, category) =>
        ratingWithLabelComponent(category.title.map(_.value).getOrElse(""))
      )
  )
