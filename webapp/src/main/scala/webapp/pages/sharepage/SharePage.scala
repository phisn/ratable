package webapp.pages.sharepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.pages.sharepage.*
import webapp.services.*
import webapp.store.aggregates.ratings.given
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.{*, given}

case class SharePage(
  ratableId: String
) extends Page:
  override def render(using services: Services): HtmlVNode =
    div(
      cls := "min-h-screen bg-base-200",
      div(
        // center content vertically and horizontally
        cls := "absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2",
        div(
          cls := "space-y-8",
          div(
            // center horizontally
            cls := "flex justify-center",
            div("image")
          ),
          div(
            cls := "text-3xl text-center",
            "Done! Now share your ratable with your friends"
          ),
          copyBox("View link", services.routing.link(SharePage(ratableId))),
          copyBox("Edit & Vote link", services.routing.link(SharePage(ratableId + "-edit")))
        )
      )
    )
