package webapp.mocks

import com.github.plokhotnyuk.jsoniter_scala.core.*
import kofre.base.*
import org.scalajs.dom
import rescala.default.*
import scala.collection.mutable.*
import webapp.services.*
import webapp.store.framework.*

class StatePersistenceServiceMock[I : JsonValueCodec : Bottom](initialID: String = "<not-used>", initial: Option[I] = None) extends StatePersistanceServiceInterface:
  val changes = ListBuffer[I]()
  
  override def storeAggregateSignal[A : JsonValueCodec : Bottom](id: String, factory: A => Signal[A]) = 
    if initialID == id then
      val signal = factory(
        DeltaContainer[I](
          inner = initial.getOrElse(Bottom[I].empty),
          delta = Bottom[I].empty
        ).asInstanceOf[A]
      )

      signal
        .map(_.asInstanceOf[DeltaContainer[I]].inner)
        .observe(change => changes.addOne(change))
        
      signal
    else
      factory(Bottom[A].empty)
