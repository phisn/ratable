package webapp.application.components.common

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.util.*
import webapp.*
import webapp.application.{given, *}
import webapp.application.framework.*
import webapp.services.*
import webapp.state.framework.*

private def starInputComponent(
  uniqueId: String, 
  isChecked: Boolean, 
  onClickEvt: Evt[Unit]
) = 
  input(
    cls := "mask mask-star-2 bg-accent bg w-10 h-10",
    tpe := "radio",
    name := uniqueId,
    checked := isChecked,
    onClick.as(()) --> onClickEvt
  )

private def starComponent(isChecked: Boolean) = 
  div(
    cls := "mask mask-star-2 bg-primary bg w-10 h-10",
    
    if isChecked then
      cls := "opacity-20"
    else
      cls := ""
  )

def ratingWithLabelComponent(
  label: String, 
  promise: PromiseSignal[Int],
  isReadOnly: Boolean = false 
) =
  val uniqueId = Random.alphanumeric.take(16).mkString

  val stars = 5
  val defaultStars = promise.default.getOrElse(3)

  val events = (0 until stars).map(_ => Evt[Unit]())

  promise :=
    Events.foldAll(defaultStars) { _ =>
      events.zipWithIndex.map((evt, index) => evt.act2(_ => index + 1))
    }.value

  div(
    cls := "flex flex-col space-y-2",
    div(
      cls := "text-2xl",
      label
    ),
    div(
      cls := "rating",
      Range(0, stars).map { i => 
        if isReadOnly then
          starComponent(i > defaultStars - 1)
        else
          starInputComponent(uniqueId, i == defaultStars - 1, events(i))
      }
    )
  )
