package webapp.store.aggregates.rating

import webapp.store.aggregates.rating.*
import webapp.store.framework.*

type RatingRepository = Repository[String, Rating]

extension (repo: RatingRepository)
  def create(id: String, ratingValue: Int, replicaID: String) =
    repo.mutate(id, _.rate(ratingValue, replicaID))
