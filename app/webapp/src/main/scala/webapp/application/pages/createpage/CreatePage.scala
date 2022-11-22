package webapp.application.pages.createpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.application.components.*
import webapp.application.components.layouts.*
import webapp.application.pages.sharepage.*
import webapp.application.pages.viewpage.*
import webapp.services.*
import webapp.state.framework.{given, *}
import webapp.application.{given, *}
import webapp.application.usecases.ratable.*

class FormValidation:
  val validateEvent = Evt[Unit]()

case class CreatePage(val title: String) extends Page:
  def render(using services: Services): VNode =
    val titleVar = Var(title)

    val validateForm = Evt[Unit]()
    val categoriesVar = Var(List(Var("")))

    layoutComponent(
      contentHorizontalCenterComponent(
        inputComponent(
          "Rating of this great chinese food place", 
          "Title of your Ratable", 
          titleVar
        ),
        categorySelectionComponent(categoriesVar),
        div(
          button(
            cls := "btn btn-primary w-full md:w-auto",
            "Create",
            onClick.foreach(_ =>
              val id = createRatable(titleVar.now, categoriesVar.now.map(_.now))
              services.routing.to(SharePage(id), true)
            )
          )
        )
      )
    )
