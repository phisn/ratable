package webapp.pages.sharepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.*
import webapp.components.icons.*
import webapp.components.layouts.*
import webapp.pages.homepage.*
import webapp.pages.ratepage.*
import webapp.pages.viewpage.*
import webapp.services.*
import webapp.store.framework.*
import webapp.{*, given}

case class SharePage(
  ratableID: String
) extends Page:
  override def render(using services: Services): HtmlVNode =
    layoutSingleRatable(ratableID)(ratable =>
      div(
        cls := "flex-grow flex flex-col",
        centerContentComponent(
          cls := "p-4",
          div(
            div(
              cls := "flex justify-center pb-16",
              iconCheckCircleFill(
                cls := "w-40 fill-accenfsdt"
              )
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
    )
