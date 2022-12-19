package core.framework.customCRDT.v13

import core.framework.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.*

// A claim is something that can be required by a event. To be able to create an event, you must prove that you have the required claims.
case class Claim[I](
  val publicKey: Array[Byte],
  val id: I,
)

object Claim:
  def create[C](claim: C)(using crypt: Crypt) =
    for
      CryptKeyValuePair(privateKey, publicKey) <- crypt.generateKey
    yield
      (
        Claim(publicKey, claim),
        ClaimProver(privateKey, claim)
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

trait Event[A, C]:
  def asEffect(context: C): Effect[A]

case class Effect[A](
  val verify:  A => Boolean,
  val advance: A => A
)

case class ContextPCmRDT[I](
  val replicaId: String,
  val claims: Set[I]
)

case class MessagePCmRDT[A, I](
  val replicaId: String,
  val event: Event[A, ContextPCmRDT[I]],
  val proofs: Set[ClaimProof[I]]
):
  def asContext =
    ContextPCmRDT(replicaId, proofs.map(_.id))

case class PCmRDT[A, I](
  val state: A,
  val claims: Set[Claim[I]]
):
  def verify(message: MessagePCmRDT[A, I])(using Crypt): Future[Boolean] =
    for 
      proofsValid <- Future.sequence(
        for
          proof <- message.proofs
          claim <- claims.find(_.id == proof.id)
        yield
          proof.verify(claim, message.replicaId)
      )

      effectValid = if proofsValid.forall(identity) then
        val effect = message.event.asEffect(message.asContext)
        effect.verify(state)
      else
        false

    yield
      effectValid

  def handle(message: MessagePCmRDT[A, I]): PCmRDT[A, I] =
    val effect = message.event.asEffect(message.asContext)
    this.copy(state = effect.advance(state))
