package core.framework

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.*
import scala.Conversion

case class ReplicaId(
  val publicKey: BinaryData
)

object ReplicaId:
  given JsonValueCodec[ReplicaId] = JsonCodecMaker.make

  given JsonKeyCodec[ReplicaId] = new JsonKeyCodec[ReplicaId]:
    def decodeKey(in: JsonReader): ReplicaId =
      ReplicaId(BinaryData(java.util.Base64.getDecoder().decode(in.readString(""))))

    def encodeKey(x: ReplicaId, out: JsonWriter): Unit =
      out.writeKey(java.util.Base64.getEncoder().encodeToString(x.publicKey.inner))

case class PrivateReplicaId(
  val publicKey: BinaryData,
  val privateKey: BinaryData
):
  def public: ReplicaId = ReplicaId(BinaryData(publicKey.inner))

object PrivateReplicaId:
  def apply()(using crypt: Crypt): Future[PrivateReplicaId] =
    crypt.generateKey.map(
      keys => PrivateReplicaId(BinaryData(keys.publicKey), BinaryData(keys.privateKey))
    )

  given Conversion[PrivateReplicaId, ReplicaId] = new Conversion[PrivateReplicaId, ReplicaId]:
    def apply(x: PrivateReplicaId): ReplicaId = x.public

  given JsonValueCodec[PrivateReplicaId] = JsonCodecMaker.make
