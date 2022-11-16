package core.messages

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

case class AggregateMessage[A](
  aggregate: A
)

object AggregateMessage:
  given [A: JsonValueCodec]: JsonValueCodec[AggregateMessage[A]] = JsonCodecMaker.make
