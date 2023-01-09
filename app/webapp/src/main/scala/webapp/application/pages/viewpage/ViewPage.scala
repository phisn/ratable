package webapp.application.pages.viewpage

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.*
import webapp.application.components.*
import webapp.application.components.common.*
import webapp.application.components.layouts.*
import webapp.application.pages.homepage.*
import webapp.application.pages.ratepage.*
import webapp.application.framework.given
import webapp.application.services.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

case class ViewPage(
  ratableId: String
) extends Page:
  val aggregateId = AggregateId.fromBase64(ratableId)

  def render(using services: ServicesWithApplication): VNode =
    layoutSingleRatable(aggregateId)(ratable =>
      contentHorizontalCenterComponent(
        titleComponent(ratable.title),
        badgesComponent(ratable),
        viewRatingsComponent(ratable)(
          cls := "md:pt-6"
        )
      )
    )
