package webapp.application.pages.createpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.application.components.*
import webapp.application.components.layouts.*
import webapp.application.framework.*
import webapp.application.pages.homepage.*
import webapp.application.pages.viewpage.*
import webapp.services.*
import webapp.state.framework.{given, *}
import webapp.application.{given, *}
import webapp.application.usecases.ratable.*

def categorySelectionComponent(
  categories: Var[List[VarWithValidation[String]]]
)(using form: FormValidation) =
  div(
    cls := "space-y-6",
    div(
      cls := "flex flex-col space-y-4",
      categories.map(_.map(categoryComponent))
    ),
    div(
      cls := "flex space-x-4",
      button(
        cls := "btn btn-outline",
        "Add category",
        onClick.foreach(_ => categories.transform(_ :+ categoryVar(""))),

        categories
          .map(_.size >= 3)
          .map(disabled := _)
      ),
      button(
        cls := "btn btn-outline",
        "Remove category",
        
        onClick
          .filter(_ => categories.now.size > 1)
          .foreach(_ => categories.transform(_.dropRight(1))),

        categories
          .map(_.size == 1)
          .map(disabled := _)
      )
    )
  )
