package webapp.application.pages.viewpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.components.*
import webapp.application.components.layouts.*
import webapp.application.pages.homepage.*
import webapp.application.pages.ratepage.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

case class ViewPage(
  ratableID: String
) extends Page:
  def render(using services: Services): VNode =
    layoutSingleRatable(ratableID)(ratable =>
      div(
        cls := "flex-grow flex justify-center p-4 md:p-12",
        div(
          cls := "flex flex-col space-y-6 w-[40rem]",
          
          titleComponent(ratable.title),
          badgesComponent(ratable),
          viewRatingsComponent(ratable)(
            cls := "md:pt-6"
          )
        )
      )
    )
