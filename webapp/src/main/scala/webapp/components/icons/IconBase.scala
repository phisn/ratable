package webapp.components.icons

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.store.aggregates.rating.{given, *}
import webapp.store.framework.*
import webapp.{*, given}

def iconBase(using services: Services) =
  import svg.* 
  svg(
    services.config.darkMode.map( darkMode => 
      if darkMode then      
        style := "filter: invert(1)"
      else
        style := ""
    )
  )
