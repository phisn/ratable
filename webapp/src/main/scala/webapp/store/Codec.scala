package webapp.store

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.store.aggregates.ratable.*
import kofre.base.Defs
import kofre.decompose.containers.DeltaBufferRDT
import kofre.dotted.Dotted
import kofre.time.Dot
import webapp.Services

given JsonValueCodec[RatableRepository] = JsonCodecMaker.make
given JsonValueCodec[ApplicationStateDTO] = JsonCodecMaker.make
