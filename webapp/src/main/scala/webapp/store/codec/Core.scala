package webapp.store.codec

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.Defs
import kofre.datatypes.GrowOnlyCounter
import kofre.decompose.containers.DeltaBufferRDT
import kofre.dotted.Dotted
import kofre.time.Dot
import webapp.Services
import webapp.store.LocalRatingState

given JsonKeyCodec[Dot] = new JsonKeyCodec[Dot] :
  override def decodeKey(in: JsonReader): Dot =
    val Array(time, id) = in.readKeyAsString().split("-", 2)
    Dot(id, time.asInstanceOf[Defs.Time])

  override def encodeKey(x: Dot, out: JsonWriter): Unit = out.writeKey(s"${x.time}-${x.replicaId}")

given (using services: Services, dottedCodec: JsonValueCodec[Dotted[LocalRatingState]]
): JsonValueCodec[DeltaBufferRDT[LocalRatingState]] = new JsonValueCodec[DeltaBufferRDT[LocalRatingState]]:
  override def decodeValue(in: JsonReader, default: DeltaBufferRDT[LocalRatingState]): DeltaBufferRDT[LocalRatingState] =
    val state = dottedCodec.decodeValue(in, default.state)
    new DeltaBufferRDT[LocalRatingState](state, services., List())

  override def encodeValue(x: DeltaBufferRDT[LocalRatingState], out: JsonWriter): Unit =
    dottedCodec.encodeValue(x.state, out)

  override def nullValue: DeltaBufferRDT[LocalRatingState] = null
