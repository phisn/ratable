package core.framework.CmRDT

import core.framework.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

// A role is something that can be required by a event. To be able to create an event, you must prove that you have the required roles.
case class Role[ID](
  val publicKey: Array[Byte],
  val id: ID,
)

object Role:
  def create[ID](id: ID)(using crypt: Crypt) =
    for
      CryptKeyValuePair(privateKey, publicKey) <- crypt.generateKey
    yield
      (
        Role(publicKey, id),
        RoleProver(privateKey, id)
      )

// A role proof is a proof that you have a role. It is only a proof for your replicaId.
case class RoleProof[ID](
  val proof: Array[Byte],
  val id: ID,
):
  // Verifies that proof is the replicaID encrypted with the private key of the role.
  def verify(role: Role[ID], replicaId: String)(using crypt: Crypt): Future[Boolean] =
    crypt.verify(role.publicKey, replicaId, proof)

// A role prover allows you to prove that any replicaId has a role.
case class RoleProver[ID](
  val privateKey: Array[Byte],
  val id: ID
):
  // Encrypts the replicaId with the private key of the role.
  def prove(replicaId: String)(using crypt: Crypt): Future[RoleProof[ID]] =
    for
      proof <- crypt.sign(privateKey, replicaId)
    yield
      RoleProof(proof, id)
