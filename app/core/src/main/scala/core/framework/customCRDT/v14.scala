package core.framework.customCRDT.v14

import core.framework.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

trait EventExtender[A, B, C1, C2]:
  def extend(event: Event[A, C1], context: C2): Event[B, C2]
  def dumpDown(context: C2): C1

given transitive[A, B, C, C1, C2, C3](using le: EventExtender[A, B, C1, C2], re: EventExtender[B, C, C2, C3]): EventExtender[A, C, C1, C3] with
  def extend(event: Event[A, C1], context: C3): Event[C, C3] =
    re.extend(le.extend(event, re.dumpDown(context)), context)
  
  def dumpDown(context: C3): C1 =
    le.dumpDown(re.dumpDown(context))

given identity[A, C]: EventExtender[A, A, C, C] with
  def extend(event: Event[A, C], context: C): Event[A, C] =
    event

  def dumpDown(context: C): C =
    context

case class Event[A, C](
  val verify:  (A, C) => Future[Boolean],
  val advance: (A, C) => Future[A]
)

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

case class PermissionExtensionContext[C, I](
  val inner: C,
  val replicaId: String,
  val claims: Set[I]
)

case class PermissionExtension[A, I](
  val inner: A,
  val claims: Set[Claim[I]]
)

given [A, C, I]: EventExtender[A, PermissionExtension[A, I], C, PermissionExtensionContext[C, I]] with
  def extend(event: Event[A, C], context: PermissionExtensionContext[C, I]): Event[PermissionExtensionContext[C, I], PermissionExtension[A, I]] =
    Event(
      (context, state) =>
        Future.sequence(
          for
            proof <- context.
            claim <- claims.find(_.id == proof.id)
          yield
            proof.verify(claim, message.replicaId)
        ),
      (_, state) => 
        event
          .advance(state.inner, context.inner)
          .map(adv => state.copy(inner = adv))
    )

  def dumpDown(context: PermissionExtensionContext[C, I]): C =
    context.inner

// val counterState = PermissionExtension[Counter, CounterClaims]
