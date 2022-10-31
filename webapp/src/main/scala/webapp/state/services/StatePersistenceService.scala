package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import kofre.base.*
import org.scalajs.dom
import rescala.default.*
import webapp.services.*

import scala.reflect.Selectable.*

trait StatePersistanceServiceInterface:
  def storeAggregateSignal[A : JsonValueCodec : Bottom](id: String, factory: A => Signal[A]): Signal[A]

class StatePersistenceService(services: {
  val logger: LoggerServiceInterface
}) extends StatePersistanceServiceInterface:
  def storeAggregateSignal[A : JsonValueCodec : Bottom](id: String, factory: A => Signal[A]) =
    val sig = factory(aggregateFromStorageOrBottom(id)(services.logger))
    
    sig.observe(
      aggregate => 
        dom.window.localStorage.setItem(id, writeToString(aggregate))
        services.logger.trace(s"Writing to storage: $id"),
      fireImmediately = true
    )
    
    sig

  private def aggregateFromStorageOrBottom[A : JsonValueCodec : Bottom](id: String)(logger: LoggerServiceInterface): A =
    val item = dom.window.localStorage.getItem(id)

    if item != null then
      try {
        logger.trace(s"Reading from storage: $id")
        return readFromString(item) 
      }
      catch {
        case cause: Throwable =>
          dom.window.localStorage.removeItem(id)

          logger.error(s"Could not restore $id: $cause")
          cause.printStackTrace()
      }
    
    Bottom[A].empty
