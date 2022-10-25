package webapp.store.framework

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*

case class GenDoc[A, B, C](a: A, opt: Option[B], list: List[C])

object GenDoc {
  implicit def make[A : JsonValueCodec, B : JsonValueCodec, C : JsonValueCodec]: JsonValueCodec[GenDoc[A, B, C]] =
    JsonCodecMaker.make
}

case class DeltaContainer[A](
  inner: A,
  delta: A
):
  def mutate(f: A => A)(using Lattice[A]) =
    val mutation = f(inner)

    DeltaContainer(
      inner = Lattice[A].merge(inner, mutation),
      delta = Lattice[A].merge(delta, mutation)
    )
    
  def applyDelta(incomingDelta: A)(using Lattice[A]) =
    DeltaContainer(
      inner = Lattice[A].merge(inner, incomingDelta),
      delta = delta
    )

object DeltaContainer:
  given [A : JsonValueCodec]: JsonValueCodec[DeltaContainer[A]] = JsonCodecMaker.make

  given [A : Bottom]: Bottom[DeltaContainer[A]] = Bottom.derived
  given [A : Bottom : DecomposeLattice]: DecomposeLattice[DeltaContainer[A]] = DecomposeLattice.derived
