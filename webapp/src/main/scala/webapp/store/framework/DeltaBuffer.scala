package webapp.store.framework

import kofre.base.DecomposeLattice

class DeltaRecorder[A : Bottom : DecomposeLattice](
  val aggregate: A,
  val delta: A
):
  def applyDelta(delta: A): DeltaRecorder[A] =
    DecomposeLattice[A].diff(aggregate, delta) match
      case Some(diff) => DeltaRecorder(
        aggregate = DecomposeLattice[A].merge(aggregate, diff),
        delta = DecomposeLattice[A].merge(delta, diff)
      )
      case None => this

  def clearDelta(): DeltaRecorder[A] = DeltaRecorder(aggregate, DecomposeLattice[A].empty)

  