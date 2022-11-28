package webapp.services

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.*

trait Popup:
  val closeEvent: Event[Unit]
  
  def render(using services: Services): VNode

class PopupService(services: Services):
  private val popupQueue = collection.mutable.Queue[Popup]()
  private val popupState = Var[Option[Popup]](None)

  def render =
    implicit val services = this.services
    popupState.map(_.map(_.render))

  def show(popup: Popup) =
    popup.closeEvent.observe(_ =>
      if popupState.now.getOrElse(null) == popup then
        if popupQueue.nonEmpty then
          popupState.set(Some(popupQueue.dequeue()))
        else
          popupState.set(None)
    )

    popupQueue.enqueue(popup)

    if popupState.now.isEmpty then
      popupState.set(Some(popupQueue.dequeue()))
