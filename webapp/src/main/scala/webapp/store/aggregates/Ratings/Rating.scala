package webapp.store.aggregates.ratings

import kofre.base.{Bottom, DecomposeLattice}
import kofre.datatypes.GrowOnlyCounter
import kofre.syntax.PermIdMutate.withID
import kofre.syntax.{ArdtOpsContains, OpsSyntaxHelper}
import webapp.store.framework.{LWW, given}

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
