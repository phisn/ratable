package webapp.application.pages.sharepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.components.*
import webapp.application.components.icons.*
import webapp.application.components.layouts.*
import webapp.application.pages.homepage.*
import webapp.application.pages.ratepage.*
import webapp.application.pages.viewpage.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

case class SharePage(
  ratableID: String
) extends Page:
  override def render(using services: Services): VNode =
    layoutSingleRatable(ratableID)(ratable =>
      contentFullCenterComponent(
        div(
          div(
            cls := "flex justify-center pb-16",
            iconCheckCircleFill(cls := "w-40")
          ),
          div(
            cls := "text-3xl text-center mb-4",
            "Done! Now share your ratable with your friends"
          ),
          copyBoxComponent(
            "View link",
            ViewPage(ratableID)
          ),
          copyBoxComponent(
            "Edit & Vote link", 
            RatePage(ratableID)
          )
        )
      )
    )
