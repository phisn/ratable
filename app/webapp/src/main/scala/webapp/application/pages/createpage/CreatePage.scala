package webapp.application.pages.createpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.application.components.*
import webapp.application.components.common.*
import webapp.application.components.layouts.*
import webapp.application.framework.*
import webapp.application.pages.sharepage.*
import webapp.application.pages.viewpage.*
import webapp.services.*
import webapp.state.framework.{given, *}
import webapp.application.{given, *}
import webapp.application.usecases.ratable.*

case class CreatePage(val title: String) extends Page:
  def render(using services: Services): VNode =
    implicit val form = FormValidation()

    val titleVar = form.validatePromise(title, _.length > 0)
    val categoriesVar = PromiseSignal(List(""))

    layoutComponent(
      contentHorizontalCenterComponent(
        inputComponent(
          "Rating of this great chinese food place", 
          "Title of your Ratable", 
          titleVar
        ),
        categorySelectionComponent(
          categoriesVar
        ),
        div(
          button(
            cls := "btn btn-primary w-full md:w-auto",
            "Create",
            onClick.filter(_ => form.validate).foreach(_ =>
              val id = createRatable(titleVar.signal.now, categoriesVar.now)
              services.routing.to(SharePage(id), true)
            )
          )
        )
      )
    )
