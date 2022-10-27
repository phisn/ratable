package core.store.framework

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import kofre.syntax.{ArdtOpsContains, OpsSyntaxHelper}
import kofre.base.DecomposeLattice
import kofre.base.DecomposeLattice.{given, *}
import kofre.base.{given, *}

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
  
  def mutate(id: ID, aggregate: A)(using Bottom[A]) =
    Repository(Map(id -> aggregate))

  def get(id: ID) =
    inner.get(id)
  
extension [A](repo: Repository[String, A])
  def uniqueID(replicaID: Defs.Id) =  
    Iterator.from(1)
      .map(_ => replicaID.take(2) + "-" + UUID.randomUUID())
      .find(!repo.inner.contains(_))
      .get

object Repository:
  given C1[A : JsonValueCodec]: JsonValueCodec[Repository[Int, A]] = JsonCodecMaker.make
  given C2[A : JsonValueCodec]: JsonValueCodec[Repository[String, A]] = JsonCodecMaker.make

  given [ID, A]: Bottom[Repository[ID, A]] = Bottom.derived
  given [ID, A : DecomposeLattice]: DecomposeLattice[Repository[ID, A]] = DecomposeLattice.derived
  
  implicit def repositoryToMap[ID, A](repo: Repository[ID, A]): Map[ID, A] = repo.inner

