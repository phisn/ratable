package webapp.application.components

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

def contentHorizontalCenterComponent(args: VModifier*) =
  div(
    cls := "flex-grow flex justify-center p-4 md:p-12",
    div(
      cls := "flex flex-col space-y-6 w-[40rem]",
      args
    )
  )
