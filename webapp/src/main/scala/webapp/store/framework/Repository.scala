package webapp.store.framework

import kofre.base.*
import kofre.syntax.{ArdtOpsContains, OpsSyntaxHelper}
import kofre.base.DecomposeLattice
import kofre.base.DecomposeLattice.DecomposeFromLattice

import scala.util.Random
import java.util.UUID

// Repositories map between aggregates A and ID, allowing easy manipulation of single aggregates
case class Repository[ID, A](
  inner: Map[ID, A]
):
  def getOrBottom(id: ID)(using Bottom[A]) = 
    inner.getOrElse(id, Bottom[A].empty)

  def mutate(id: ID, f: A => A)(using Bottom[A]) =
    Repository(Map(id -> f(getOrBottom(id))))

extension [A](repo: Repository[String, A])
  def uniqueID(replicaID: Defs.Id) =  
    Iterator.from(1)
      .map(_ => replicaID.take(2) + "-" + UUID.randomUUID())
      .find(!repo.inner.contains(_))
      .get

given [A]: Bottom[Repository[String, A]] = new Bottom[Repository[String, A]] {
  def empty = Repository(Map.empty)
}

given [ID, A](using inner: DecomposeLattice[Map[ID, A]]): DecomposeLattice[Repository[ID, A]] with
  override def lteq(left: Repository[ID, A], right: Repository[ID, A]): Boolean = inner.lteq(left.inner, right.inner)
  override def decompose(state: Repository[ID, A]): Iterable[Repository[ID, A]] = inner.decompose(state.inner).map(Repository(_))
  override def merge(left: Repository[ID, A], right: Repository[ID, A]): Repository[ID, A] = Repository(inner.merge(left, right))

/*
given [A]: DecomposeLattice[Set[A]] = new DecomposeFromLattice[Set[A]](Lattice.setLattice) {
  override def lteq(left: Set[A], right: Set[A]): Boolean = left subsetOf right
  override def decompose(state: Set[A]): Iterable[Set[A]] = state.map(Set(_))
}
*/
// given [A]: DecomposeLattice[Repository[String, A]] = DecomposeLattice.mapLattice

implicit def repositoryToMap[ID, A](repo: Repository[ID, A]): Map[ID, A] = repo.inner
