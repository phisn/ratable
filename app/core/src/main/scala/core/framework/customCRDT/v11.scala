package core.framework.customCRDT.v10

import core.framework.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

// extendable replicated data type

enum CounterRoles:
  case Adder

case class Counter(
  val value: Int
)

case class AdderEvent(
  val value: Int
)


