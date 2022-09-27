package webapp.store.framework

import kofre.base.Bottom
import kofre.datatypes.TimedVal

// Simple LastWriteWins Register
type LWW[A] = Option[TimedVal[A]]

given [A]: Bottom[LWW[A]] = new Bottom[LWW[A]] {
  def empty = None
}

extension [A](lww: LWW[A])
  def read: Option[A] = lww match
    case Some(v) => Some(v.value)
    case None => None

object LWW:
  def empty[A] = None
  def apply[A](value: A, replicaID: String): LWW[A] =
    Some(TimedVal.apply(value, replicaID))
