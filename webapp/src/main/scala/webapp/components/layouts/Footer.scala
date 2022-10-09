package webapp.components.layouts

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
        img(
          cls := "w-6 h-6",
          src := "/icons/github.svg",
        )
      )
    )
  )
