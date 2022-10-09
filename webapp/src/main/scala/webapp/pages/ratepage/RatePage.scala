package webapp.pages.ratepage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.store.aggregates.ratings.given
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.{*, given}

case class RatePage(
  ratableId: String
) extends Page:
  def render(using services: Services): HtmlVNode =
    div(
      "Rate"
    )
