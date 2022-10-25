package webapp.mocks

import com.github.plokhotnyuk.jsoniter_scala.core.*
import kofre.base.*
import org.scalajs.dom
import rescala.default.*
import scala.collection.mutable.*
import webapp.services.*

class StatePersistenceServiceMock[I : JsonValueCodec : Bottom](initialID: String = "<not-used>", initial: Option[I] = None) extends StatePersistanceServiceInterface:
  val changes = ListBuffer[I]()
  
  override def storeAggregateSignal[A : JsonValueCodec : Bottom](id: String, factory: A => Signal[A]) = 
    if initialID == id then
      val signal = factory(initial.map(_.asInstanceOf[A]).getOrElse(Bottom[A].empty))
      signal.observe(change => changes.addOne(change.asInstanceOf[I]))
      signal
    else
      factory(Bottom[A].empty)
