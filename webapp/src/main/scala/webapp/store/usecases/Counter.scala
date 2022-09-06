package webapp.store.usecases

import kofre.datatypes.GrowOnlyCounter
import kofre.decompose.containers.DeltaBufferRDT
import rescala.default.*
import webapp.store.LocalRatingState
import webapp.store.aggregates.Counter

def incrementCounterUsecase(rdt: DeltaBufferRDT[LocalRatingState]): DeltaBufferRDT[LocalRatingState] =
  LocalRatingState(Counter(Map(rdt.replicaID -> (rdt.state.getOrElse(state.replicaID, 0) + 1)))).mutator
