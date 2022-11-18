package webapp.application.components.layouts

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.components.icons.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def footerComponent(using services: Services) =
  footer(
    cls := "footer grid-cols-3 bg-base-200 p-2",
    div(
      cls := "col-start-2 flex place-self-center",
      div(
        "made by ",
        a(
          cls := "font-bold",
          href := "https://github.com/phisn",
          target := "_blank",
          "phisn"
        )
      )
    ),
    div(
      cls := "col-start-3 grid-flow-col justify-self-end",
      a(
        cls := "transition hover:bg-gray-400 rounded p-2",
        href := "https://github.com/phisn/local-rating",
        target := "_blank",
        iconGithub(
          cls := "w-6 h-6",
        )
      )
    )
  )
