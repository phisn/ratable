package webapp.application.pages.drawer

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.util.*
import webapp.{given, *}
import webapp.application.*
import webapp.application.components.common.*
import webapp.application.framework.given
import webapp.application.services.*
import webapp.services.*
import webapp.state.framework.*

// https://stackoverflow.com/a/65447216/9185797
// https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API
// https://tailwindcss.com/docs/animation#pulse

def infiniteScroll(using services: ServicesWithApplication): VNode =
  infiniteScroll(() => Retrievals(Seq(testNode, testNode, testNode, testNode, testNode), true))

def testNode =
  div(
    cls := "flex justify-between w-full space-x-4",
    div(
      // with random string added
      s"Title ${Random.alphanumeric.take(10).mkString}"
    ),
    div(
      ratingComponent(4)
    )
  )

case class Retrievals(
  val items: Seq[VNode],
  val hasMore: Boolean
)

def infiniteScroll(retrieve: () => Retrievals)(using services: ServicesWithApplication) =
  val items = Var(Seq[VNode]())

  items.transform(_ ++ retrieve().items)
  items.transform(_ ++ retrieve().items)

  div(
    cls := "flex flex-col items-center space-y-2",
    items
  )
