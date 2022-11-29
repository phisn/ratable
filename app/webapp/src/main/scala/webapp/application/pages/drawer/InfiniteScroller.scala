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

case class Retrievals(
  val items: Seq[VNode],
  val hasMore: Boolean
)

def infiniteScrollerComponent(retrieve: () => Future[Retrievals])(using services: ServicesWithApplication) =
  services.logger.trace("Infinitescroller: Opening new scroller")

  val itemsVar = Var(Seq[VNode]())
  val loadedVar = Var(false)

  var loading: Future[Unit] = Future.successful(())
  var intersecting = false
  
  def retrieveHelper(observer: IntersectionObserver): Future[Unit] =
    retrieve()
      .andThen {
        case Success(retrievals) if !retrievals.hasMore =>
          services.logger.trace(s"Infinitescroller: Loaded")
          loadedVar.set(true)

        case Failure(exception) =>
          services.logger.error(s"Infinitescroller: Error while retrieving items: $exception")
      }
      .flatMap(retrievals =>
        services.logger.trace(s"Infinitescroller: Retrieved ${retrievals.items.size} items")

        itemsVar.transform(_ ++ retrievals.items)

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

      if !loadedVar.now && intersecting && loading.isCompleted then
        loading = retrieveHelper(observer)
  )

  itemsVar.observe(
    items =>
      services.logger.trace(s"Infinitescroller: Updating items to ${items.size}")
  )

  div(
    cls := "flex flex-col items-center space-y-6 md:space-y-6",
    Signal { (itemsVar.value, loadedVar.value) }.map((items, loaded) =>
      if loaded && items.size == 0 then
        div(
          cls := "",
          "No Ratables yet"
        )
      else
        div(
          idAttr := "infinite-scroll",
          loadedVar.map(loaded =>
            if loaded then
              div(
                items
              )
            else
              import svg.*
              div(
                cls := "flex justify-center items-center",
                svg(
                  cls := "animate-spin w-8 h-8",
                  viewBox := "0 0 24 24",
                  fill := "none",
                  path(fill := "currentColor", d := """M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z""")
                )
              )
          ),
          onDomMount.foreach { _ =>
            val element = dom.document.getElementById("infinite-scroll")

            observer.observe(element)
            
            loadedVar.changed.filter(x => x).observe(_ =>
              observer.disconnect()
            )
          }
        )
    )
  )
