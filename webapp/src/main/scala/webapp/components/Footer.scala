package webapp.components

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.store.aggregates.ratings.given
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.{*, given}

def footerComponent(using services: Services) =
  footer(
    cls := "footer items-center p-4 bg-base-300 text-base-content",
    div(
      cls := "grid-flow-col flex",
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
      cls := "grid-flow-col justify-self-end place-self-center",
      a(
        cls := "transition hover:bg-gray-400 rounded p-2",
        src := "",
        img(
          cls := "w-6 h-6",
          src := "/icons/github.svg",
        )
      )
    )
  )
