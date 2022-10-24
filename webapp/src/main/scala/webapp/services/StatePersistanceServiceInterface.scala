package webapp.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import kofre.base.*
import org.scalajs.dom
import scala.reflect.Selectable.*
import rescala.default.*

trait StatePersistanceServiceInterface:
  def storeAggregateSignal[A : JsonValueCodec : Bottom](id: String, factory: A => Signal[A]): Signal[A]
