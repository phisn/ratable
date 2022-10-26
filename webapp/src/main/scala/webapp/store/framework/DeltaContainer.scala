package webapp.store.framework

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*

case class TaggedDelta[A](
  tag: Int, 
  delta: A
)

object TaggedDelta:
  given [A : JsonValueCodec]: JsonValueCodec[TaggedDelta[A]] = JsonCodecMaker.make

case class DeltaContainer[A](
  inner: A,
  deltas: Set[TaggedDelta[A]]
):
  def mutate(f: A => A)(using Lattice[A]) =
    val mutation = f(inner)

    DeltaContainer(
      inner = Lattice[A].merge(inner, mutation),
      deltas = deltas + TaggedDelta(uniqueTag, mutation)
    )
    
  def applyDelta(incomingDelta: A)(using Lattice[A]) =
    DeltaContainer(
      inner = Lattice[A].merge(inner, incomingDelta),
      deltas = deltas
    )

  def uniqueTag =
    deltas.map(_.tag).maxOption.getOrElse(0) + 1

object DeltaContainer:
  given [A : JsonValueCodec]: JsonValueCodec[DeltaContainer[A]] = JsonCodecMaker.make

  given [A : Bottom]: Bottom[DeltaContainer[A]] = Bottom.derived
  given [A : Bottom : DecomposeLattice]: DecomposeLattice[DeltaContainer[A]] = DecomposeLattice.derived
