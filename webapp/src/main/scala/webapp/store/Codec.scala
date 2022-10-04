package webapp.store

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonKeyCodec, JsonReader, JsonValueCodec, JsonWriter}
import kofre.base.Defs
import kofre.decompose.containers.DeltaBufferRDT
import kofre.dotted.Dotted
import kofre.time.Dot
import webapp.Services
import webapp.store.aggregates.ratings.*
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

given JsonValueCodec[Ratings] = JsonCodecMaker.make
given JsonValueCodec[ApplicationStateDTO] = JsonCodecMaker.make
