package core.framework

import com.github.plokhotnyuk.jsoniter_scala.core.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.*
import scala.Conversion

case class ReplicaId(
  val publicKey: Array[Byte]
)

object ReplicaId:
  given JsonValueCodec[ReplicaId] = new JsonValueCodec[ReplicaId]:
    def decodeValue(in: JsonReader, default: ReplicaId): ReplicaId =
      ReplicaId(java.util.Base64.getDecoder().decode(in.readString("")))

    def encodeValue(x: ReplicaId, out: JsonWriter): Unit =
      out.writeVal(java.util.Base64.getEncoder().encodeToString(x.publicKey))

    def nullValue: ReplicaId =
      ReplicaId(Array.empty[Byte])

  given JsonKeyCodec[ReplicaId] = new JsonKeyCodec[ReplicaId]:
    def decodeKey(in: JsonReader): ReplicaId =
      ReplicaId(java.util.Base64.getDecoder().decode(in.readString("")))

    def encodeKey(x: ReplicaId, out: JsonWriter): Unit =
      out.writeKey(java.util.Base64.getEncoder().encodeToString(x.publicKey))

case class PrivateReplicaId(
  val publicKey: Array[Byte],
  val privateKey: Array[Byte]
):
  def public: ReplicaId = ReplicaId(publicKey)

object PrivateReplicaId:
  def apply()(using crypt: Crypt): Future[PrivateReplicaId] =
    crypt.generateKey.map(
      keys => PrivateReplicaId(keys.publicKey, keys.privateKey)
    )

  given Conversion[PrivateReplicaId, ReplicaId] = new Conversion[PrivateReplicaId, ReplicaId]:
    def apply(x: PrivateReplicaId): ReplicaId = x.public

  given JsonValueCodec[PrivateReplicaId] = new JsonValueCodec[PrivateReplicaId]:
    def decodeValue(in: JsonReader, default: PrivateReplicaId): PrivateReplicaId =
      if in.nextToken() != '[' then in.arrayStartOrNullError()
      val publicKey = java.util.Base64.getDecoder().decode(in.readString(""))
      if in.nextToken() != ',' then in.commaError()
      val privateKey = java.util.Base64.getDecoder().decode(in.readString(""))
      if in.nextToken() != ']' then in.arrayEndOrCommaError()
      PrivateReplicaId(publicKey, privateKey)

    def encodeValue(x: PrivateReplicaId, out: JsonWriter): Unit =
      out.writeArrayStart()
      out.writeVal(java.util.Base64.getEncoder().encodeToString(x.publicKey))
      out.writeVal(java.util.Base64.getEncoder().encodeToString(x.privateKey))
      out.writeArrayEnd()

    def nullValue: PrivateReplicaId =
      PrivateReplicaId(Array.empty[Byte], Array.empty[Byte])
