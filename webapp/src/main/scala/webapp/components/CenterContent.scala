package webapp.components

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.store.aggregates.rating.{given, *}
import webapp.store.framework.*
import webapp.{*, given}

def centerContentComponent(html: HtmlVNode) =
  div(
    cls := "flex-grow flex justify-center items-center",
    html
  )
