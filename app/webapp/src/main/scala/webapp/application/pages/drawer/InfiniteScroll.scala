package webapp.application.pages.drawer

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.*
import webapp.{given, *}
import webapp.application.*
import webapp.application.components.common.*
import webapp.application.pages.viewpage.*
import webapp.application.framework.given
import webapp.application.services.*
import webapp.services.*
import webapp.state.framework.*
import typings.std.IntersectionObserver
import typings.std.IntersectionObserverInit

// https://stackoverflow.com/a/65447216/9185797
// https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API
// https://tailwindcss.com/docs/animation#pulse

def infiniteScroll(using services: ServicesWithApplication): VNode =
  var counter = 100
  infiniteScroll(() => 
    counter -= 1
    Future.successful(Retrievals(
      if counter > 0 then
        Seq(testNode)
      else
        Seq.empty
      , counter > 0)))

def testNode(using services: ServicesWithApplication) =
  div(
    cls := "flex flex-col items-center w-full space-y-2",
    a(
      cls := "flex flex-col transition hover:bg-base-200 rounded-xl p-4",
      div(
        cls := "text-xl",
        // with random string added
        s"Title ${Random.alphanumeric.take(10).mkString}"
      ),
      div(
        cls := "flex items-center w-full space-x-6",
        ratingComponent(4),
        div(
          cls := "badge badge-outline",
          "123"
        )
      ),
      href := services.routing.link(ViewPage("test"))
    )
  )

case class Retrievals(
  val items: Seq[VNode],
  val hasMore: Boolean
)

def infiniteScroll(retrieve: () => Future[Retrievals])(using services: ServicesWithApplication) =
  val items = Var(Seq[VNode]())

  items.transform(_ ++ retrieve().value.get.get.items)
  items.transform(_ ++ retrieve().value.get.get.items)

  val loadedVar = Var(false)

  var loading: Future[Unit] = Future.successful(())
  var intersecting = false
  
  def retrieveHelper(observer: IntersectionObserver): Future[Unit] =
    retrieve()
      .andThen {
        case Success(retrievals) if !retrievals.hasMore =>
          loadedVar.set(true)
      }
      .flatMap(retrievals =>
        items.transform(_ ++ retrievals.items)

        if retrievals.hasMore && intersecting && observer.takeRecords().forall(_.isIntersecting) then        
          val promise = Promise[Unit]()
          
          scala.scalajs.js.timers.setTimeout(0) {
            promise.completeWith(retrieveHelper(observer))
          }

          promise.future

        else
          Future.successful(())
      )

  val observer = typings.std.global.IntersectionObserver(
    (entries, observer) =>
      intersecting = entries.exists(_.isIntersecting)

      if intersecting && loading.isCompleted then
        loading = retrieveHelper(observer)
  )

  div(
    cls := "flex flex-col items-center space-y-6 md:space-y-8",
    items,
    div(
      idAttr := "infinite-scroll",
      loadedVar.map(loaded =>
        if loaded then
          div(
            "No more items"
          )
        else
          div(
            cls := "flex items-center justify-center w-full h-12",
            div(
              cls := "spinner",
            )
          )
      ),
      onSnabbdomInsert.foreach { _ =>
        observer.observe(dom.document.getElementById("infinite-scroll"))
      }
    )
  )
