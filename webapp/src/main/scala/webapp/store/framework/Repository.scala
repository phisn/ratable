package webapp.store.framework

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

case class Repository[ID, A](
  inner: Map[ID, A]
) derives DecomposeLattice, Bottom:
  def getOrBottom(id: ID)(using Bottom[A]) = inner.getOrElse(id, Bottom[A].empty)

implicit def repositoryToMap[ID, A](repo: Repository[ID, A]): Map[ID, A] = repo.inner

implicit class RepositorySyntax[C, ID, A: Bottom](container: C)(using ArdtOpsContains[C, Repository[ID, A]])
  extends OpsSyntaxHelper[C, Repository[ID, A]](container):
  def mutate(id: ID, mutation: A => A)(using MutationP): C =
    Repository(Map(id -> mutation(current.getOrBottom(id)))).mutator
