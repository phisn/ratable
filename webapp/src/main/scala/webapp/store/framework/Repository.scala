package webapp.store.framework

import kofre.base.*
import kofre.syntax.{ArdtOpsContains, OpsSyntaxHelper}

import scala.util.Random

// Repositories map between aggregates A and ID, allowing easy manipulation of single aggregates
case class Repository[ID, A](
  inner: Map[ID, A]
) derives DecomposeLattice, Bottom:
  def getOrBottom(id: ID)(using Bottom[A]) = inner.getOrElse(id, Bottom[A].empty)
  
extension [A](repo: Repository[Long, A])
  def uniqueID(replicaID: Defs.Id) =
    val prefix = replicaID.hashCode() << 32
    val suffix =
      Iterator.from(1).map(_ => Random.nextInt()).find(suffix =>
        !repo.inner.contains(suffix + prefix)
      ).get

    suffix + prefix

implicit def repositoryToMap[ID, A](repo: Repository[ID, A]): Map[ID, A] = repo.inner

implicit class RepositorySyntax[C, ID, A](container: C)(using ArdtOpsContains[C, Repository[ID, A]], Bottom[A])
  extends OpsSyntaxHelper[C, Repository[ID, A]](container):
  def mutate(id: ID, mutation: A => A)(using MutationP): C =
    Repository(Map(id -> mutation(current.getOrBottom(id)))).mutator
