package webapp.application.components

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.*
import webapp.application.components.icons.*
import webapp.application.framework.given
import webapp.application.pages.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def footerComponent(using services: ServicesWithApplication) =
  footer(
    cls := "footer grid-cols-4 bg-base-200 p-1 px-2 md:p-2",
    div(
      cls := "flex self-center p-2",
      /*
      a(
        "Privacy Policy",
        href := services.routing.linkPath(PrivacyPage())
      )
      */
      ""
    ),
    div(
      cls := "flex place-self-center col-span-2",
      div(
        services.local.get("component.footer"),
        a(
          cls := "font-bold",
          href := "https://github.com/phisn",
          target := "_blank",
          "phisn"
        )
      )
    ),
    div(
      cls := "grid-flow-col justify-self-end",
      a(
        cls := "transition hover:bg-gray-400 rounded p-1 md:p-2",
        href := "https://github.com/phisn/local-rating",
        target := "_blank",
        iconGithub(
          cls := "w-5 h-5 md:w-6 md:h-6",
        )
      )
    )
  )
