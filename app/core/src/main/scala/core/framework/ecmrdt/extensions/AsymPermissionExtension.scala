package core.framework.ecmrdt.extensions

import core.framework.*
import core.framework.ecmrdt.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

trait AsymPermissionContextExtension[I]:
  val proofs: Set[ClaimProof[I]]

  def claims = proofs.map(_.id)

  def verifyClaim(claim: I) =
    Option.unless(proofs.exists(_.id == claim))(s"Missing claim $claim.")
  
trait AsymPermissionStateExtension[I]:
  val claims: Set[Claim[I]]

/*
def verifyEffectPipeline[A, C](effect: Effect[A, C])(validators: (Option[String] | Future[Option[String]])*): (A, C) => Future[Option[String]] =
  verifyEffectPipeline(effect)(validators.toSet)
*/

object AsymPermissionEffectPipeline:
  def apply[A <: AsymPermissionStateExtension[I], I, C <: AsymPermissionContextExtension[I] with IdentityContext](using Crypt): EffectPipeline[A, C] =
    verifyEffectPipelineFuture[A, C]((state, context) =>
      val x = for
        proof <- context.proofs
        claim <- state.claims.find(_.id == proof.id)
      yield
        proof.verify(claim, context.replicaId).map(
          Option.unless(_)(s"Invalid proof for claim ${claim.id}.")
        )

      x + Future.successful(Option.unless(context.proofs.exists(x => state.claims.exists(_.id == x.id)))
        ("Claim does not exist."))
    )
