package webapp.application.pages.homepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.*
import webapp.application.components.*
import webapp.application.components.icons.*
import webapp.application.components.layouts.*
import webapp.application.components.popups.*
import webapp.application.framework.given
import webapp.application.services.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

case class HomePage() extends Page:
  def render(using services: ServicesWithApplication): VNode =
    if dom.window.localStorage.getItem("wasOpened") == null then
      val popup = InfoPopup()

      popup.closeEvent.observe(
        _ => dom.window.localStorage.setItem("wasOpened", "true")
      )

      services.popup.show(popup)

    layoutCustomHeaderComponent(
      headerCleanComponent
    )(
      contentFullCenterComponent(
        div(
          cls := "space-y-8 md:space-y-12 md:w-[45rem]",
          h1(
            // Title
            cls := "text-5xl font-bold text-center",
            services.local.get("page.home.header")
          ),
          ratableInputComponent,
          div(
            // Longer description
            cls := "flex flex-col items-center space-y-4 text-lg text-center pt-2",
/*            iconInfo(
              cls := "w-8 h-8"
            ),
*/
//            "A Ratable is a thing that can be rated. It can be a restaurant, a movie, a book or anything else you can think of. Share your Ratable with your friends and let them rate it."
          ),
        )
      )
    )
