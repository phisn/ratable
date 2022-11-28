package webapp.application.pages.debugpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*
import webapp.application.framework.{given, *}
import webapp.services.*
import webapp.state.framework.*
import webapp.Services

import scala.util.*

def functionsTest(using services: Services) = 
    val inputStr = Var("")
    val outputStr: Var[Signal[Option[String]]] = Var(Var(None))

    div(
        cls := "flex space-x-4",
        button(
            cls := "btn btn-primary",
            "Test functions",
            //onClick.map(_ => services.backendApi.hello(inputStr.now)) --> outputStr
        ),
        input(
            cls := "input input-bordered",
            placeholder := "Enter your name",
            onInput.value --> inputStr
        ),
        div(
            outputStr.flatten[Signal[Option[String]]]
        )
    )
