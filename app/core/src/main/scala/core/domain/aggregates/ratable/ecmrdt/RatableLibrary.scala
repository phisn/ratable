package core.domain.aggregates.ratable.ecmrdt

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.framework.*
import core.framework.ecmrdt.*
import core.framework.ecmrdt.extensions.*

case class RatableLibrary(
  val replicaId: ReplicaId,
  val passwords: Map[String, String]

) extends SingleOwnerStateExtension

object RatableLibrary:
  given EffectPipeline[RatableLibrary, RatableLibraryContext] = EffectPipeline(
    SingleOwnerEffectPipeline()
  )

case class RatableLibraryContext(
  val replicaId: ReplicaId,
) extends IdentityContext
