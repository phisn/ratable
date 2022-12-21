package core.framework.ecmrdt

import core.framework.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

// A claim is something that can be required by a event. To be able to create an event, you must prove that you have the required claims.
case class Claim[I](
  val publicKey: Array[Byte],
  val id: I,
)

object Claim:
  def create[C](claimIds: Set[C])(using crypt: Crypt): Future[(Set[Claim[C]], Set[ClaimProver[C]])] =
    Future.sequence(
        claimIds.map(create(_))
      )
      .map(_.unzip)

  def create[C](claimId: C)(using crypt: Crypt): Future[(Claim[C], ClaimProver[C])] =
    for
      CryptKeyValuePair(privateKey, publicKey) <- crypt.generateKey
    yield
      (
        Claim(publicKey, claimId),
        ClaimProver(privateKey, claimId)
      )

// A claim proof is a proof that you have a claim. It is only a proof for your replicaId.
case class ClaimProof[C](
  val proof: Array[Byte],
  val id: C,
):
  // Verifies that proof is the replicaID encrypted with the private key of the claim.
  def verify(claim: Claim[C], replicaId: String)(using crypt: Crypt): Future[Boolean] =
    crypt.verify(claim.publicKey, replicaId, proof)

// A claim prover allows you to prove that any replicaId has a claim.
case class ClaimProver[ID](
  val privateKey: Array[Byte],
  val id: ID
):
  // Encrypts the replicaId with the private key of the claim.
  def prove(replicaId: String)(using crypt: Crypt): Future[ClaimProof[ID]] =
    for
      proof <- crypt.sign(privateKey, replicaId)
    yield
      ClaimProof(proof, id)

trait ClaimRegistry[I]:
  def proof(claim: I): Future[ClaimProof[I]]

// Helper to build events with proofs easily
def withProofs[A, I](claims: I*)(f: Set[ClaimProof[I]] => A)(using registry: ClaimRegistry[I]) =
  for
    proofs <- Future.sequence(claims.map(registry.proof))
  yield
    f(proofs.toSet)
