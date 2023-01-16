package core.framework.ecmrdt.extensions

import core.framework.*
import core.framework.ecmrdt.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

object SingleOwnerEffectPipeline:
  def apply[A, C <: IdentityContext](): EffectPipeline[A, C] =
    verifyEffectPipeline[A, C]((state, context, meta) => List(
      Option.when(meta.ownerReplicaId != context.replicaId)(
        RatableError(s"Replica ${context.replicaId} is not the owner ${meta.ownerReplicaId} of this state.")
      )
    ))
