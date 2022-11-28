package webapp.application.pages.createpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.application.*
import webapp.application.components.*
import webapp.application.components.common.*
import webapp.application.components.layouts.*
import webapp.application.framework.*
import webapp.application.pages.homepage.*
import webapp.application.pages.viewpage.*
import webapp.services.*
import webapp.state.framework.{given, *}
import webapp.application.framework.{given, *}
import webapp.application.usecases.ratable.*

def categoryComponent(categoryTitle: PromiseSignalWithValidation[String])(using form: FormValidation, services: ServicesWithApplication) =
  Signal {
    (
      services.local.get("page.create.categoryInput.label").value,
      services.local.get("page.create.categoryInput.placeholder").value
    )
  }.map((label, placeholder) =>
    inputComponent(
      placeholder, 
      label,
      categoryTitle
    )
  )
