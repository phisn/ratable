package webapp.store.selectors

import rescala.default.*
import webapp.store.LocalRatingState

def selectCount(state: Signal[LocalRatingState]): Signal[Int] = state.map(_.counter.inner.valuesIterator.sum)
