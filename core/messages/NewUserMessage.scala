package core.messages

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

case class NewUserMessage(
  connectionString: String
)

given JsonValueCodec[NewUserMessage] = JsonCodecMaker.make