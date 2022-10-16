package webapp.pages.debugpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.store.aggregates.rating.{given, *}
import webapp.store.framework.*
import webapp.Services
import webapp.given
import webapp.usecases.rating.*

import scala.util.*

def functionsTest(using services: Services) = 
    val inputStr = Var("")
    val outputStr: Var[Signal[Option[String]]] = Var(Var(None))

    div(
        cls := "flex space-x-4",
        button(
            cls := "btn btn-primary",
            "Test functions",
            onClick.map(_ => services.backendApi.hello(inputStr.now)) --> outputStr
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
