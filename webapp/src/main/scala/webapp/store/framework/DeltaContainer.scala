package webapp.store.framework

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.store.framework.*
import kofre.base.*

case class DeltaContainer[A](
  inner: A,
  deltas: Set[TaggedDelta[A]]
):
  // Mutation by client
  def mutate(f: A => A)(using Lattice[A]) =
    val mutation = f(inner)

    DeltaContainer(
      inner = Lattice[A].merge(inner, mutation),
      deltas = deltas + TaggedDelta(uniqueTag, mutation)
    )
  
  // Delta received from the server
  def applyDelta(incomingDelta: A)(using Lattice[A]) =
    DeltaContainer(
      inner = Lattice[A].merge(inner, incomingDelta),
      deltas = deltas
    )

  // Prepares merged delta for sending to the server
  def mergedDeltas(using Lattice[A], Bottom[A]) =
    TaggedDelta(
      deltas.map(_.tag).maxOption.getOrElse(0L),
      deltas.foldLeft(Bottom[A].empty)((acc, delta) => Lattice[A].merge(acc, delta.delta))
    )

  // Acknowledges delta received from the server
  def acknowledge(tag: Tag) =
    DeltaContainer(
      inner = inner,
      deltas = deltas.filter(_.tag > tag)
    )
 
  private def uniqueTag =
    deltas.map(_.tag).maxOption.getOrElse(0L) + 1L

object DeltaContainer:
  given [A : JsonValueCodec]: JsonValueCodec[DeltaContainer[A]] = JsonCodecMaker.make

  given [A : Bottom]: Bottom[DeltaContainer[A]] = Bottom.derived
  given [A : Bottom : DecomposeLattice]: DecomposeLattice[DeltaContainer[A]] = DecomposeLattice.derived
