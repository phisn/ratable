package webapp.store

import kofre.decompose.containers.DeltaBufferRDT
import webapp.store.aggregates.Counter

case class LocalRatingState(
  counter: DeltaBufferRDT[Counter]
)
