package core.framework

import com.github.plokhotnyuk.jsoniter_scala.core.*
import scala.scalajs.js

extension [A : JsonValueCodec](a: A)
  def toJs = 
    js.JSON.parse(writeToString(a))

extension (a: js.Any)
  def toScala[A : JsonValueCodec] =
    readFromString[A](js.JSON.stringify(a))
  def asInstanceOfOr[A](default: A) = a.asInstanceOf[js.UndefOr[A]].toOption.getOrElse(default)
