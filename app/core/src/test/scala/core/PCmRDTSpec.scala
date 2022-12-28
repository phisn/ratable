package core

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.messages.common.*
import core.framework.{*, given}
import core.framework.pcmrdt.*
import org.scalatest.*
import org.scalatest.flatspec.*
import org.scalatest.matchers.should.Matchers.*
import kofre.base.*
import _root_.scala.concurrent.*
import _root_.scala.concurrent.ExecutionContext.Implicits.global
import core.framework.ecmrdt.*
import core.framework.ecmrdt.example.*
import _root_.scala.util.Random

/*
given Crypt with
  def generateKey: Future[CryptKeyValuePair] =
    val r = Random.nextInt().toByte
    Future.successful(
      CryptKeyValuePair(Array[Byte](r, 1), Array[Byte](r, 2)) 
    )

  def sign(key: Array[Byte], content: String): Future[Array[Byte]] =
    Future.successful(
      Array[Byte](key(0), key(1), content.hashCode.toByte)
    )

  def verify(key: Array[Byte], content: String, signature: Array[Byte]): Future[Boolean] =
    val c1 = signature(0) == key(0)
    val c2 = signature(1) == 1
    val c3 = key(1) == 2 
    val c4 = signature(2) == content.hashCode.toByte
    
    // println(s"verify: $c1 && $c2 && $c3 && $c4")

    Future.successful(
      c1 && c2 && c3 && c4
    )
*/

sealed trait SomeType:
  def print: Unit

case class Test1(
) extends SomeType:
  def print: Unit =
    println("test1")

case class Test2(
) extends SomeType:
  def print: Unit =
    println("test2")

given JsonValueCodec[SomeType] = JsonCodecMaker.make

class TestSpec extends AsyncFlatSpec:
  implicit override def executionContext = _root_.scala.concurrent.ExecutionContext.Implicits.global
  val t: SomeType = Test1()
  val k = readFromString[SomeType]("{\"type\":\"Test2\"}")
  k.print

    /*
    // ! start

    val (counter, provers) = PCmRDT.create(Counter(0), CounterRoles.values)

    val eventSource = EventSource[CounterRoles](
      replicaId,
      proofs = provers.map(_.prove(replicaId).value.get.get)
    )
    
    // ! user wants to increment counter

    val addEvent = AddEvent(1)
    val addEffect = addEvent.asEffect(eventSource)
    
    val newCounter = addEffect.advance(counter.state)

    newCounter.inner.value should be (1)

    val newCounter2 = addEffect.advance(newCounter)

    newCounter2.inner.value should be (2)
    */
