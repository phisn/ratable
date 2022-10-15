package webapp.store.aggregates.rating

import kofre.base.{Bottom, DecomposeLattice}
import kofre.datatypes.GrowOnlyCounter
import kofre.syntax.PermIdMutate.withID
import kofre.syntax.{ArdtOpsContains, OpsSyntaxHelper}
import webapp.store.framework.{*, given}

import scala.util.Random

case class Rating(
  ratableId: 
) derives DecomposeLattice, Bottom:
  def rate(ratingValue: Int, replicaID: String): Rating =
    Rating(value = LWW.apply(ratingValue, replicaID))
