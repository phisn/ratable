package core.store.framework

import kofre.base.Bottom
import kofre.datatypes.TimedVal
import kofre.base.DecomposeLattice
import kofre.base.DecomposeLattice.DecomposeFromLattice

// Simple LastWriteWins Register
type LWW[A] = Option[TimedVal[A]]

given [A]: Bottom[LWW[A]] = new Bottom[LWW[A]] {
  def empty = None
}

// common lattices
given DecomposeLattice[Long] = new DecomposeFromLattice[Long](_ max _) {
  override def lteq(left: Long, right: Long): Boolean = left <= right
  override def decompose(state: Long): Iterable[Long] = List(state)
}

given Bottom[Long] with { override def empty: Long = Long.MinValue }

extension [A](lww: LWW[A])
  def read: Option[A] = lww match
    case Some(v) => Some(v.value)
    case None => None

object LWW:
  def empty[A] = None
  def apply[A](value: A, replicaID: String): LWW[A] =
    Some(TimedVal.apply(value, replicaID))
