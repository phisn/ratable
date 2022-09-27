package webapp.store.aggregates

import kofre.base.{Bottom, DecomposeLattice, Defs}
import kofre.dotted.{DotFun, Dotted}
import kofre.decompose.interfaces.LWWRegisterInterface
import kofre.decompose.interfaces.LWWRegisterInterface.LWWRegister
import kofre.syntax.{ArdtOpsContains, OpsSyntaxHelper}
import kofre.datatypes.GrowOnlyCounter
import kofre.decompose.interfaces.MVRegisterInterface.MVRegisterSyntax
import kofre.syntax.PermIdMutate.withID
import kofre.decompose.interfaces.LWWRegisterInterface.LWWRegisterSyntax
import webapp.store.aggregates.LWW
import kofre.datatypes.GrowOnlyCounter
import webapp.store.framework.*
import scala.util.Random

case class Rating(
  votes: GrowOnlyCounter = GrowOnlyCounter.zero,
  value: LWW[Int] = LWW.empty,
) derives DecomposeLattice, Bottom

implicit class RatingSyntax[C](container: C)(using ArdtOpsContains[C, Rating])
  extends OpsSyntaxHelper[C, Rating](container):
  def rate(value: Int)(using MutationIdP): C =
    Rating(
      value = LWW.apply(value, replicaID)
    ).mutator

  def vote(using MutationIdP): C =
    Rating(
      votes = current.votes.inc()(using withID(replicaID))
    ).mutator

/*
case class Ratings(
  inner: Map[Int, Rating] = Map()
)

implicit class RatingsSyntax[C](container: C)(using ArdtOpsContains[C, Ratings])
  extends OpsSyntaxHelper[C, Ratings](container):

  def rate(ratingID: Int, value: Int)(using MutationIdP): C =
    Ratings(Map(
      ratingID -> Rating(
        value = LWW.apply(value, replicaID)
      )
    )).mutator

  def vote(ratingID: Int)(using MutationIdP): C = 
    mutateRating(
      ratingID,
      rating => Rating(
        votes = rating.votes.inc()(using withID(replicaID))
      )
    )
  
  private def mutateRating(ratingID: Int, rating: Rating => Rating)(using MutationP): C =
    Ratings(Map(ratingID -> rating(current.inner.getOrElse(ratingID, Rating())))).mutator
*/

/*
implicit class RatingsSyntax[C](container: C)(using aoc: ArdtOpsContains[C, Ratings])
  extends OpsSyntaxHelper[C, Ratings](container) {
  def average(using QueryP): Int = current.inner.valuesIterator.sum / current.inner.size

  def rate(value: Int)(using MutationIdP): C =
    assert(value >= 0 && value <= 5, "Rating must be between 0 and 5")
    Ratings(Map(replicaID -> value)).mutator
}
*/