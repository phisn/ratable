package webapp.application.components.layouts

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.application.*
import webapp.application.framework.{given, *}
import webapp.application.components.*
import webapp.application.components.icons.*
import webapp.application.services.*
import webapp.application.pages.drawer.*
import webapp.services.*
import webapp.state.framework.*
import webapp.{*, given}
import org.scalajs.dom.HTMLInputElement

def layoutCustomHeaderComponent(header: VNode)(body: VNode)(using services: ServicesWithApplication) =
  div(
    cls := "drawer drawer-end",
    input(
      idAttr := "main-drawer",
      tpe := "checkbox",
      cls := "drawer-toggle",
      services.routing.stateSignal.map(state =>
        services.logger.trace(s"LayoutCustomHeaderComponent: state = ${state.drawerOpened}")
        checked := state.drawerOpened
      ),
      onChange.foreach(event =>
        services.logger.trace("LayoutCustomHeaderComponent: drawer toggle changed")
        if event.target.asInstanceOf[HTMLInputElement].checked then
          services.routing.toStateOnly(RoutingState(
            canReturn = services.routing.state.canReturn,
            drawerOpened = true
          ))
        else
          if services.routing.state.drawerOpened then
            services.routing.back
      )
    ),
    div(
      cls := "drawer-content flex flex-col min-h-screen bg-base-100",
      header,
      div(
        cls := "flex-grow flex flex-col",
        body
      ),
      footerComponent
    ),
    drawer,
    services.popup.render.map(_.map(popup =>
      div(
        popup
      )
    ))
  )
