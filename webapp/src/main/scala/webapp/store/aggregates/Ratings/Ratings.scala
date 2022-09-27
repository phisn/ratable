package webapp.store.aggregates.ratings

import kofre.syntax.PermIdMutate.withID
import kofre.syntax.ArdtOpsContains
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*

import scala.util.Random

type Ratings = Repository[Int, Rating]

implicit class RatingsSyntax[C](container: C)(using ArdtOpsContains[C, Ratings]) extends RepositorySyntax[C, Int, Rating](container):
  def rate(value: Int)(using MutationIdP): C =
    val ratingID = Random.nextInt()
    mutate(ratingID, rating => rating.rate(value)(using withID(replicaID)))

  def vote(ratingID: Int)(using MutationIdP): C = 
    mutate(ratingID, rating => rating.vote(using withID(replicaID)))
