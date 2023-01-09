package core.framework

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

case class BinaryData(
  val inner: Array[Byte]
):
  override def equals(x: Any): Boolean =
    x match
      case BinaryData(inner) =>
        this.inner.sameElements(inner)
      case _ =>
        false

object BinaryData:
  given JsonValueCodec[BinaryData] = new JsonValueCodec[BinaryData]:
    def decodeValue(in: JsonReader, default: BinaryData): BinaryData =
      BinaryData(java.util.Base64.getDecoder().decode(in.readString("")))

    def encodeValue(x: BinaryData, out: JsonWriter): Unit =
      out.writeVal(java.util.Base64.getEncoder().encodeToString(x.inner))

    def nullValue: BinaryData =
      BinaryData(Array.empty[Byte])
