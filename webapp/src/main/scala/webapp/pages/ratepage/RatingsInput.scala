package webapp.pages.ratepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.components.*
import webapp.components.layouts.*
import webapp.pages.viewpage.*
import webapp.services.*
import webapp.store.aggregates.rating.{given, *}
import webapp.store.framework.*
import webapp.{*, given}

def ratingsInputComponent =
  div(
    cls := "flex flex-col space-y-6 items-center md:items-start",
    ratingWithLabelComponent("Taste"),
    ratingWithLabelComponent("Ambiente"),
    ratingWithLabelComponent("Price"),
  )
