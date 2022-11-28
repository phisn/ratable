package webapp.application.components.popups

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.application.*
import webapp.application.components.icons.*
import webapp.application.framework.given
import webapp.application.services.*
import webapp.application.framework.given
import webapp.services.*

class InfoPopup extends Popup:
  val closeEvt = Evt[Unit]()
  val closeEvent = closeEvt
  
  def render(using services: ServicesWithApplication) =
    div(
      cls := "absolute top-0 left-0 w-full h-full flex items-center justify-center",
      div(
        cls := "bg-base-100 rounded-lg shadow-lg m-2 p-4 w-[48rem] z-10",
        div(
          cls := "flex items-center justify-between",
          div(
            cls := "btn btn-square btn-ghost",
            iconArrowLeftShort(
              cls := "w-8 h-8",
              onClick.foreach(_ => closeEvt.fire())
            )
          ),
          iconInfo(
            cls := "w-8 h-8 mr-4"
          )
        ),
        div(
          cls := "p-4 space-y-4",
          div(
            cls := "text-xl",
            strong("Ratable"),
            services.local.get("component.popup.info.text")
          ),
          div(
            cls := "text-sm",
            services.local.get("component.popup.info.hint")
          )
        ),
      ),
      div(
        cls := "absolute top-0 left-0 w-full h-full bg-black bg-opacity-50 z-0",
        onClick.foreach(_ => closeEvt.fire())
      ),
    )
