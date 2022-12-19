package core.framework.customCRDT.v10

import core.framework.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

// Layer 1: ReplicaId verification

case class Replica(
  val publicKey: Array[Byte],
  val id: String
)

case class Message(
  val replica: Replica,
  val signature: Array[Byte],
  val content: String
)

def deserialize(message: Message)(using crypt: Crypt): Future[Option[LowerLayer]] =
  for
    verified <- crypt.verify(message.replica.publicKey, message.content, message.signature)
  yield
    if verified then
      Some(deserialize(message.content))
    else
      None

def deserialize(content: String): LowerLayer =
  ???

// Layer 2: Claim verification

trait LowerLayer
