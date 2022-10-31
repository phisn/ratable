package webapp.pages.debugpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.util.*
import webapp.{given, *}
import webapp.services.*
import webapp.state.framework.*

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

case class DebugPage() extends Page:
  def render(using services: Services): VNode =
    div(
      cls := "p-4 space-y-16",
      clickCounter,
      functionsTest,
      createRating,
      ratings,
      jsonApplicationState,
      div(
        sys.props.map(i => div(i(0), " = ", i(1))).toList
      ),
      div(
        sys.env.map(i => div(i(0), " = ", i(1))).toList
      ),
      div(
        services.config.backendUrl
      )
    )

def jsonApplicationState(using services: Services) =
  div(
    services.state.application.toSignalDTO.map(dto => writeToString(dto))
  )
