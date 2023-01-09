package webapp.application.pages.sharepage

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.*
import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.*
import webapp.application.components.*
import webapp.application.components.icons.*
import webapp.application.components.layouts.*
import webapp.application.pages.homepage.*
import webapp.application.pages.ratepage.*
import webapp.application.pages.viewpage.*
import webapp.application.framework.given
import webapp.application.services.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}

case class SharePage(
  ratableId: String
) extends Page:
  val aggregateId = AggregateId.fromBase64(ratableId)

  override def render(using services: ServicesWithApplication): VNode =
    layoutSingleRatable(aggregateId)(ratable =>
      contentFullCenterComponent(
        div(
          div(
            cls := "flex justify-center pb-16",
            iconCheckCircleFill(cls := "w-40")
          ),
          div(
            cls := "text-3xl text-center mb-4",
            services.local.get("page.share.title")
          ),
          services.local.get("page.share.viewLink").map(label =>
            copyBoxComponent(
              label,
              ViewPage(ratableId)
            ),
          ),
          services.local.get("page.share.rateLink").map(label =>
            copyBoxComponent(
              label,
              RatePage(ratableId)
            ),
          )
        )
      )
    )
