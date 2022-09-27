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
import webapp.store.framework.aggregates.*
import scala.util.Random

type Ratings = Repository[Int, Rating]

implicit class RatingsSyntax[C](container: C)(using ArdtOpsContains[C, Ratings]) extends RepositorySyntax[C, Int, Rating](container):
  def rate(value: Int)(using MutationIdP): C =
    val ratingID = Random.nextInt()
    mutate(ratingID, rating => rating.rate(value)(using withID(replicaID)))

  def vote(ratingID: Int)(using MutationIdP): C = 
    mutate(ratingID, rating => rating.vote(using withID(replicaID)))