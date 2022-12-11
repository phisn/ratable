package core

import core.messages.common.*
import core.framework.{*, given}
import core.framework.pcmrdt.*
import org.scalatest.*
import org.scalatest.flatspec.*
import org.scalatest.matchers.should.Matchers.*
import kofre.base.*
import _root_.scala.concurrent.*
import _root_.scala.concurrent.ExecutionContext.Implicits.global

enum CounterRoles:
  case Adder

case class Counter(
  val value: Int
)

trait CounterEvent extends GenericEvent[Counter, CounterRoles]

case class AddEvent(
  val value: Int
):
  def asEffect(source: EventSource[CounterRoles]): Effect[Counter, CounterRoles] =
    Effect(
      verifyRoles(source, CounterRoles.Adder),
      mutateWithoutContext(state =>
        state.copy(value = state.value + value)
      )

//      (state) => source.verifyProofs(state.findRoles(CounterRoles.Adder)),
//      (state) => WithContext(state.inner.copy(value = state.inner.value + value), state.roles)
    )

given Crypt with
  def generateKey: Future[CryptKeyValuePair] =
    Future.successful(
      CryptKeyValuePair(Array[Byte](1), Array[Byte](2)) 
    )

  def sign(key: Array[Byte], content: String): Future[Array[Byte]] =
    Future.successful(
      Array[Byte](key(0), content.hashCode.toByte)
    )

  def verify(key: Array[Byte], content: String, signature: Array[Byte]): Future[Boolean] =
    Future.successful(
      signature(0) == key(0) && signature(1) == content.hashCode.toByte
    )

class TestSpec extends AsyncFlatSpec:
  implicit override def executionContext = _root_.scala.concurrent.ExecutionContext.Implicits.global

  def prepare(replicaId: String) =
    for
      (counter, provers) <- PCmRDT.create(Counter(0), CounterRoles.values)
      proofs <- Future.sequence(provers.map(_.prove(replicaId)))
    yield
      (counter, EventSource[CounterRoles](replicaId, proofs), provers)

  "PCmRDT" should "be created and advance with effect" in {
    for
      (counter, source, provers) <- prepare("replicaId")

      addEvent = AddEvent(1)
      addEffect = addEvent.asEffect(source)

      newCounter = addEffect.advance(counter.state)

      newCounter2 = addEffect.advance(newCounter)
    yield
      newCounter.inner.value should be (1)
      newCounter2.inner.value should be (2)
  }

  "Effect" should "verify proof" in {
    for
      (counter, source, provers) <- prepare("replicaId")

      addEvent = AddEvent(1)
      addEffect = addEvent.asEffect(source)

      verifyEffect <- addEffect.verify(counter.state)


    yield
  }
    

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
