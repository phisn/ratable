package core.messages

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

// Used by client and server for up and down stream deltas
case class DeltaMessage(
  aggregateId: String,
  delta: String
)

object DeltaMessage:
  given JsonValueCodec[DeltaMessage] = JsonCodecMaker.make

  def apply[A : JsonValueCodec](aggregateId: String, delta: A): DeltaMessage =
    DeltaMessage(aggregateId, writeToString(delta))
