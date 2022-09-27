package webapp.store.framework

import kofre.base.{Bottom, DecomposeLattice}
import kofre.syntax.{ArdtOpsContains, OpsSyntaxHelper}

case class Repository[ID, A](
  inner: Map[ID, A]
) derives DecomposeLattice, Bottom:
  def getOrBottom(id: ID)(using Bottom[A]) = inner.getOrElse(id, Bottom[A].empty)

implicit def repositoryToMap[ID, A](repo: Repository[ID, A]): Map[ID, A] = repo.inner

implicit class RepositorySyntax[C, ID, A: Bottom](container: C)(using ArdtOpsContains[C, Repository[ID, A]])
  extends OpsSyntaxHelper[C, Repository[ID, A]](container):
  def mutate(id: ID, mutation: A => A)(using MutationP): C =
    Repository(Map(id -> mutation(current.getOrBottom(id)))).mutator
