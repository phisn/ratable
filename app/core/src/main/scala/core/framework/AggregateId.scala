package core.framework

import com.github.plokhotnyuk.jsoniter_scala.core.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.*
import scala.Conversion

case class AggregateId(
  val replicaId: ReplicaId,
  val randomBytes: Array[Byte]
)

object AggregateId:
  def apply(replicaId: ReplicaId): AggregateId =
    AggregateId(
      replicaId,
      scala.util.Random.nextBytes(16)
    )

  given valueCodec: JsonValueCodec[AggregateId] = new JsonValueCodec[AggregateId]:
    def decodeValue(in: JsonReader, default: AggregateId): AggregateId =
      in.readString("").split(":") match
        case Array(replica, random) =>
          AggregateId(
            ReplicaId(BinaryData(java.util.Base64.getDecoder().decode(replica))),
            java.util.Base64.getDecoder().decode(random)
          )
        case _ =>
          in.decodeError("expected ':'")
          
    def encodeValue(x: AggregateId, out: JsonWriter): Unit =
      val replica = java.util.Base64.getEncoder().encodeToString(x.replicaId.publicKey.inner)
      val random = java.util.Base64.getEncoder().encodeToString(x.randomBytes)
      out.writeVal(s"$replica:$random")

    def nullValue: AggregateId =
      AggregateId(ReplicaId(BinaryData(Array.emptyByteArray)), Array.emptyByteArray)

  given JsonKeyCodec[AggregateId] = new JsonKeyCodec[AggregateId]:
    def decodeKey(in: JsonReader): AggregateId =
      valueCodec.decodeValue(in, valueCodec.nullValue)

    def encodeKey(x: AggregateId, out: JsonWriter): Unit =
      valueCodec.encodeValue(x, out)

