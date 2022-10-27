package core.store.framework

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*

case class TaggedDelta[A](
  tag: Tag,
  delta: A
)

object TaggedDelta:
  given [A : JsonValueCodec]: JsonValueCodec[TaggedDelta[A]] = JsonCodecMaker.make
