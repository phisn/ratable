package webapp.store

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.Defs
import kofre.decompose.containers.DeltaBufferRDT
import kofre.dotted.Dotted
import kofre.time.Dot
import webapp.Services
import webapp.store.aggregates.rating.*
import webapp.store.aggregates.ratable.*

given JsonValueCodec[RatingRepository] = JsonCodecMaker.make
given JsonValueCodec[RatableRepository] = JsonCodecMaker.make

given JsonValueCodec[ApplicationStateDTO] = JsonCodecMaker.make
