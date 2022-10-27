package functions

import scala.scalajs.js
import scala.scalajs.js.annotation._

import scala.scalajs.js
import scala.scalajs.js.annotation._

/*
class DistributeResult(
  val broadcast: js.Any,
  val broadcastTarget: js.Any,
  val acknowledge: js.Any,
) extends js.Object
*/

class Test(
  val test: String,
) extends js.Object

object Distribute {
  @JSExportTopLevel("distribute")
  def distribute() =
    Test("Hello world")
}