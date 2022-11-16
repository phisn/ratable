package webapp.state.framework

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.state.framework.*
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

  // Used for offline compression. When user has no internet we only want to
  // send one small delta after going online again. 
  def deflateDeltas(using Lattice[A], Bottom[A]) =
    DeltaContainer(
      inner = inner,
      deltas = Set(mergedDeltas)
    )

  // Acknowledges delta received from the server
  def acknowledge(tag: Tag) =
    DeltaContainer(
      inner = inner,
      deltas = deltas.filter(_.tag > tag)
    )

  def maxTag =
    deltas.map(_.tag).maxOption.getOrElse(0L)
 
  private def uniqueTag =
    maxTag + 1L

object DeltaContainer:
  def apply[A](inner: A): DeltaContainer[A] =
    DeltaContainer(inner, Set.empty)

  given [A : JsonValueCodec]: JsonValueCodec[DeltaContainer[A]] = JsonCodecMaker.make

  given [A : Bottom]: Bottom[DeltaContainer[A]] = Bottom.derived
  given [A : Bottom : DecomposeLattice]: DecomposeLattice[DeltaContainer[A]] = DecomposeLattice.derived
