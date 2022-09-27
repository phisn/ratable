package webapp.store.aggregates.ratings

import kofre.syntax.PermIdMutate.withID
import kofre.syntax.{ArdtOpsContains, OpsSyntaxHelper}
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*

import scala.util.Random

type Ratings = Repository[Long, Rating]

implicit class RepositorySyntax[C](container: C)(using ArdtOpsContains[C, Repository[Long, Rating]])
  extends OpsSyntaxHelper[C, Repository[Long, Rating]](container):
  def insert(id: Long, ratingValue: Int)(using MutationIdP): C =
    container.mutate(id, _ => Rating().rate(ratingValue)(using withID(replicaID)))
