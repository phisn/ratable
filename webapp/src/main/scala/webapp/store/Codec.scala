package webapp.store

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonKeyCodec, JsonReader, JsonValueCodec, JsonWriter}
import kofre.base.Defs
import kofre.decompose.containers.DeltaBufferRDT
import kofre.dotted.Dotted
import kofre.time.Dot
import loci.transmitter.IdenticallyTransmittable
import webapp.Services

given JsonKeyCodec[Dot] = new JsonKeyCodec[Dot] :
  override def decodeKey(in: JsonReader): Dot =
    val Array(time, id) = in.readKeyAsString().split("-", 2)
    Dot(id, time.asInstanceOf[Defs.Time])

  override def encodeKey(x: Dot, out: JsonWriter): Unit = out.writeKey(s"${x.time}-${x.replicaId}")

given [A](using services: Services, codec: JsonValueCodec[Dotted[A]]): JsonValueCodec[DeltaBufferRDT[A]] = new JsonValueCodec[DeltaBufferRDT[A]]:
  override def decodeValue(in: JsonReader, default: DeltaBufferRDT[A]): DeltaBufferRDT[A] =
    val state = codec.decodeValue(in, default.state)
    new DeltaBufferRDT[A](state, services.config.replicaID, List())

  override def encodeValue(x: DeltaBufferRDT[A], out: JsonWriter): Unit =
    codec.encodeValue(x.state, out)

  override def nullValue: DeltaBufferRDT[A] = null

given [A]: IdenticallyTransmittable[Dotted[A]] = IdenticallyTransmittable()
