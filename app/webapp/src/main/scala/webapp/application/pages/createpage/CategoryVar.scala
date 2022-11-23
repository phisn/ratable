package webapp.application.pages.createpage

import rescala.default.*
import webapp.application.framework.*

def categoryVar(title: String = "")(using form: FormValidation) =
  form.validateVar[String](Var(title), _.length > 0)
