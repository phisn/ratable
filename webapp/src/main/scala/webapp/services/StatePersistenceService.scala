package webapp.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import org.scalajs.dom
import scala.reflect.Selectable.*
import rescala.default.*

import core.store.framework.{*, given}
import kofre.base.*
import kofre.datatypes.*
import kofre.syntax.*

case class Category(
  val title: LWW[String] = LWW.empty,

) derives DecomposeLattice, Bottom

case class Rating(
  val ratingForCategory: Map[Int, LWW[Int]],

) derives DecomposeLattice, Bottom

case class Test(
  val categories: Map[Int, String]
)

given c: JsonValueCodec[Test] = JsonCodecMaker.make

class StatePersistenceService(services: {
}):
  def storeAggregateSignal[A : JsonValueCodec : Bottom : Lattice](id: String, factory: A => Signal[A]) =
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
