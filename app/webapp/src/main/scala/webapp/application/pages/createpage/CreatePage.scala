package webapp.application.pages.createpage

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.util.*
import scala.concurrent.ExecutionContext.Implicits.global
import webapp.*
import webapp.application.*
import webapp.application.components.*
import webapp.application.components.common.*
import webapp.application.components.layouts.*
import webapp.application.framework.*
import webapp.application.pages.sharepage.*
import webapp.application.pages.viewpage.*
import webapp.application.framework.given
import webapp.application.services.*
import webapp.device.framework.given
import webapp.services.*
import webapp.state.framework.{given, *}
import webapp.application.framework.{given, *}
import webapp.application.usecases.ratable.*

case class CreatePage(val title: String) extends Page:
  def render(using services: ServicesWithApplication): VNode =
    implicit val form = FormValidation()

    val titleVar = form.validatePromise(title, _.length > 0)
    val categoriesVar = PromiseSignal(List(""))

    layoutComponent(
      contentHorizontalCenterComponent(
        Signal { 
          (
            services.local.get("page.create.titleInput.label").value,
            services.local.get("page.create.titleInput.placeholder").value,
          )
        }.map((title, placeholder) => 
          inputComponent(
            placeholder,
            title,
            titleVar
          )
        ),
        categorySelectionComponent(
          categoriesVar
        ),
        div(
          button(
            cls := "btn btn-primary w-full md:w-auto",
            services.local.get("page.home.input.button"),
            onClick.filter(_ => form.validate).foreach(_ =>
              createRatable(titleVar.signal.now, categoriesVar.now).value
                .andThen {
                  case Success(Right(id)) =>
                    services.routing.to(SharePage(id.toBase64), RoutingState(canReturn = true))

                  case Success(Left(some)) =>
                    services.logger.error(s"Create error message '${some.default}'")

                  case Failure(exception) =>
                    throw exception

                  case _ =>
                    services.logger.log(s"Create succeeded")
                }
            )
          )
        )
      )
    )
