package webapp.store

import webapp.store.aggregates.Counter

case class LocalRatingState(
  counter: Counter = Counter()
)
