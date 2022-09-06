package webapp.store.codec

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import kofre.datatypes.GrowOnlyCounter
import kofre.decompose.containers.DeltaBufferRDT
import kofre.dotted.Dotted
import kofre.time.Dot
import loci.transmitter.IdenticallyTransmittable

given JsonValueCodec[Dotted[GrowOnlyCounter]] = JsonCodecMaker.make
given IdenticallyTransmittable[Dotted[GrowOnlyCounter]] = IdenticallyTransmittable()
