package core.framework.ecmrdt.extensions

import cats.data.*
import cats.implicits.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.framework.*
import core.framework.ecmrdt.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

// The idea behind this extension is simple. We are already using the asymmetric cryptography extension
// with claims. We now encrypt each claim prover with a password. This way, each user with access to the
// password can prove their claim and do specific actions.

object ClaimBehindPassword:
  def apply(privateKey: Array[Byte], password: String)(using crypt: Crypt): Future[BinaryDataWithIV] =
    crypt.wrapKey(privateKey, password)

  given JsonValueCodec[BinaryDataWithIV] = JsonCodecMaker.make

trait ClaimByPasswordStateExtension[I]:
  def claimsBehindPassword: Map[I, BinaryDataWithIV]

  def proveByPassword(replicaId: ReplicaId, claim: I, password: String)(using crypt: Crypt): EitherT[Future, RatableError, ClaimProof[I]] =
    for
      prover <- proverFromPassword(claim, password)
      proof <- EitherT.liftF(prover.prove(replicaId))
    yield
      proof
  
  def proverFromPassword(claim: I, password: String)(using crypt: Crypt): EitherT[Future, RatableError, ClaimProver[I]] =
    for
      claimProverWraped <- EitherT.fromOption[Future](
        claimsBehindPassword.get(claim), 
        RatableError(s"Claim '${claim}' behind password not found")
      )

      claimProver <- crypt.unwrapKey(claimProverWraped, password).toRight(
        RatableError(s"Could not unwrap claim '${claim}' behind password")
      )

    yield
      ClaimProver[I](
        BinaryData(claimProver),
        claim
      )

// We do not need any context or pipeline for this extension. It is just a state extension.
