package webapp.store.aggregates

import kofre.datatypes.TimedVal

type LWW[A] = Option[TimedVal[A]]

object LWW:
  def empty[A] = None
  def apply[A](value: A, replicaID: String): LWW[A] =
    Some(TimedVal.apply(value, replicaID))
