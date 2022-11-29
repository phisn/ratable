package webapp.application.components.common

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.util.*
import webapp.*
import webapp.application.framework.{given, *}
import webapp.application.framework.*
import webapp.services.*
import webapp.state.framework.*

def ratingWithLabelInputComponent(
  label: String, 
  promise: PromiseSignal[Int]
) =
  val uniqueId = Random.alphanumeric.take(16).mkString

  val stars = 5
  val defaultStars = promise.default.getOrElse(stars / 2 + 1)

  val events = (0 until stars).map(_ => Evt[Unit]())

  promise :=
    Events.foldAll(defaultStars) { _ =>
      events.zipWithIndex.map((evt, index) => evt.act2(_ => index + 1))
    }.value

  ratingWithLabelContainerComponent(
    label,
    Range(0, stars).map(i => 
      starInputComponent(uniqueId, i == defaultStars - 1, events(i))
    )
  )

def ratingWithLabelComponent(
  label: String, 
  value: Int
) =
  val stars = 5

  ratingWithLabelContainerComponent(
    label,
    Range(0, stars).map(i => 
      starComponent(i > value - 1)
    )
  )

def ratingComponent(
  value: Int
) =
  val stars = 5
  
  div(
    cls := "rating",
    Range(0, stars).map(i => 
      div(
        cls := "mask mask-star-2 bg-primary bg w-6 h-6",
        
        if i > value - 1 then
          cls := "opacity-20"
        else
          cls := ""
      )
    )
  )

def ratingWithLabelContainerComponent(label: String, content: Seq[VNode]) =
  div(
    cls := "flex flex-col space-y-2",
    div(
      cls := "text-2xl",
      label
    ),
    div(
      cls := "rating",
      content
    )
  )

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
