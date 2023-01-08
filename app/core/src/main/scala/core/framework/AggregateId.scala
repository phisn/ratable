package core.framework

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.*
import scala.Conversion
import scalapb.TypeMapper

case class AggregateId(
  val replicaId: ReplicaId,
  val randomBytes: Array[Byte]
):
  def toBase64: String =
    java.util.Base64.getEncoder().encodeToString(replicaId.publicKey.inner) + ":" + java.util.Base64.getEncoder().encodeToString(randomBytes)

object AggregateId:
  given TypeMapper[String, AggregateId] = TypeMapper(readFromString(_))(writeToString(_))

  def singleton(replicaId: ReplicaId): AggregateId =
    AggregateId(
      replicaId,
      Array.emptyByteArray
    )

  def unique(replicaId: ReplicaId): AggregateId =
    AggregateId(
      replicaId,
      scala.util.Random.nextBytes(16)
    )

  def fromBase64(base64: String): Option[AggregateId] =
    base64.split(":") match
      case Array(replica, random) =>
        Some(
          AggregateId(
            ReplicaId(BinaryData(java.util.Base64.getDecoder().decode(replica))),
            java.util.Base64.getDecoder().decode(random)
          )
        )
      case _ =>
        None

  given JsonValueCodec[AggregateId] = new JsonValueCodec[AggregateId]:
    def decodeValue(in: JsonReader, default: AggregateId): AggregateId =
      fromBase64(in.readString("")).getOrElse(in.decodeError("expected ':'"))
          
    def encodeValue(x: AggregateId, out: JsonWriter): Unit =
      out.writeVal(x.toBase64)

    def nullValue: AggregateId =
      AggregateId(ReplicaId(BinaryData(Array.emptyByteArray)), Array.emptyByteArray)

  given JsonKeyCodec[AggregateId] = new JsonKeyCodec[AggregateId]:
    def decodeKey(in: JsonReader): AggregateId =
      fromBase64(in.readKeyAsString()).getOrElse(in.decodeError("expected ':'"))

    def encodeKey(x: AggregateId, out: JsonWriter): Unit =
      out.writeKey(x.toBase64)
