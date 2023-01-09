package core.framework.ecmrdt.extensions

import core.framework.*
import core.framework.ecmrdt.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

trait SingleOwnerStateExtension:
  def replicaId: ReplicaId
  
object SingleOwnerEffectPipeline:
  def apply[A <: SingleOwnerStateExtension, C <: IdentityContext](): EffectPipeline[A, C] =
    verifyEffectPipeline[A, C]((state, context) => 
      println(s"SingleOwnerEffectPipeline: ${state.replicaId} == ${context.replicaId}} = ${state.replicaId == context.replicaId}")
      List(
        Option.unless(state.replicaId == context.replicaId)
          (RatableError(s"Replica ${context.replicaId} is not the owner ${state.replicaId} of this object."))
      ))
