package webapp.pages.sharepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.layouts.*
import webapp.pages.ratepage.*
import webapp.services.*
import webapp.store.aggregates.ratings.given
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.{*, given}

case class SharePage(
  ratableId: String
) extends Page:
  override def render(using services: Services): HtmlVNode =
    layoutCentered(
      div(
        div(
          // center horizontally
          cls := "flex justify-center pb-16",
          img(
            cls := "w-40",
            src := "/icons/check-circle-fill.svg"
          )
        ),
        div(
          cls := "text-3xl text-center mb-4",
          "Done! Now share your ratable with your friends"
        ),
        copyBox(
          "View link", 
          services.routing.link(RatePage(ratableId))
        ),
        copyBox(
          "Edit & Vote link", 
          services.routing.link(RatePage(ratableId + "-edit"))
        )
      )
    )
