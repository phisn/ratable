package core.messages

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.store.framework.*

case class AcknowledgeDeltaMessage(
  aggregateId: String,
  tag: Tag
)

object AcknowledgeDeltaMessage:
  given JsonValueCodec[AcknowledgeDeltaMessage] = JsonCodecMaker.make
