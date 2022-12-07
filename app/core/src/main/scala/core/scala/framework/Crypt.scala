package core.scala.framework

import org.scalajs.*

trait Crypt

given Crypt with
  def encrypt(key: String, plainText: String): String =
    ???
