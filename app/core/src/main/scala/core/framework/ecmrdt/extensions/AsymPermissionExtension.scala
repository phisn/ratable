package core.framework.ecmrdt.extensions

import cats.data.*
import cats.implicits.*
import core.framework.*
import core.framework.ecmrdt.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

trait AsymPermissionContextExtension[I]:
  def proofs: List[ClaimProof[I]]

  def claims = proofs.map(_.id)

  def verifyClaim[A](claim: I): EitherT[Future, RatableError, Unit] = EitherT.cond[Future](
    proofs.exists(_.id == claim), (),
    RatableError(s"Missing claim $claim.")
  )

    // Option.unless(proofs.exists(_.id == claim))(RatableError(s"Missing claim $claim."))
  
trait AsymPermissionStateExtension[I]:
  def claims: List[Claim[I]]

// The Asymmetric Permission Extension allows permission to be aquired by replicas themselves using claim provers
// aka private keys. This allows for example a data type to be edited via a link that can be shared with other replicas.
object AsymPermissionEffectPipeline:
  def apply[A <: AsymPermissionStateExtension[I], I, C <: AsymPermissionContextExtension[I] with IdentityContext](using Crypt): EffectPipeline[A, C] =
    verifyEffectPipelineFuture[A, C]((state, context, meta) =>
      // Should create "Proof does not exist." error.
      // Should create "Proof is invalid." error.
      for
        proof <- context.proofs
      yield
        state.claims.find(_.id == proof.id) match
          case Some(claim) => 
            OptionT(proof
              .verify(claim, context.replicaId)
              .map(Option.unless(_)(RatableError("Proof is invalid.")))
            )

          case None => 
            OptionT.pure(RatableError("Claim does not exist."))
    )
