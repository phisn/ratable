package webapp.application.components.icons

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.application.framework.{given, *}
import webapp.services.*
import webapp.state.framework.*

def iconBase(using services: Services) =
  import svg.* 
  svg(
    services.config.darkMode.map(darkMode => 
      if darkMode then      
        style := ""
      else
        style := ""
    )
  )
