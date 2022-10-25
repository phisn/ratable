package core.messages

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

case class WebPubSubConnectionMessage(
  url: String,
)

object WebPubSubConnectionMessage:
  given JsonValueCodec[WebPubSubConnectionMessage] = JsonCodecMaker.make
