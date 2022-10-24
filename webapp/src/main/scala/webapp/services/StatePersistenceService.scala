package webapp.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import kofre.base.*
import org.scalajs.dom
import scala.reflect.Selectable.*
import rescala.default.*

class StatePersistenceService(services: {
}) extends StatePersistanceServiceInterface:
  def storeAggregateSignal[A : JsonValueCodec : Bottom](id: String, factory: A => Signal[A]) =
    val sig = factory(aggregateFromStorageOrBottom(id))
    
    sig.observe(
      aggregate => dom.window.localStorage.setItem(id, writeToString(aggregate)),
      fireImmediately = true
    )
    
    sig

  private def aggregateFromStorageOrBottom[A : JsonValueCodec : Bottom](id: String): A =
    val item = dom.window.localStorage.getItem(id)

    if item != null then
      try { 
        return readFromString(item) 
      }
      catch {
        case cause: Throwable =>
          dom.window.localStorage.removeItem(id)

          println(s"Could not restore $id: $cause")
          cause.printStackTrace()
      }
    
    Bottom[A].empty
