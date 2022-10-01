package webapp.store.aggregates.ratings

import kofre.syntax.PermIdMutate.withID
import kofre.syntax.{ArdtOpsContains, OpsSyntaxHelper}
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*

import scala.util.Random

type Ratings = Repository[String, Rating]

extension (repo: Ratings)
  def create(id: String, ratingValue: Int, replicaID: String) =
    repo.mutate(id, _.rate(ratingValue, replicaID))

