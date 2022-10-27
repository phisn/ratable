package core.messages

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

// Used by client and server for up and down stream deltas
case class _DeltaMessage(
  aggregateId: String,
  delta: String
)

object _DeltaMessage:
  given JsonValueCodec[_DeltaMessage] = JsonCodecMaker.make

  def apply[A : JsonValueCodec](aggregateId: String, delta: A): _DeltaMessage =
    _DeltaMessage(aggregateId, writeToString(delta))
