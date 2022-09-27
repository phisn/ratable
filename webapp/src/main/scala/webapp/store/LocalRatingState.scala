package webapp.store

import webapp.store.aggregates.*
import webapp.store.framework.*

case class LocalRatingState(
  ratings: RepositoryRDT[Ratings]
)
