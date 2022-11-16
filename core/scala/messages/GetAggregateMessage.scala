package core.messages

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.state.*

case class GetAggregateMessage(
  aggregateId: AggregateId,
)

object GetAggregateMessage:
  given JsonValueCodec[GetAggregateMessage] = JsonCodecMaker.make
