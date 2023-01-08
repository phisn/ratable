package core.framework.ecmrdt.extensions

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

  def proveByPassword(replicaId: ReplicaId, claim: I, password: String)(using crypt: Crypt): Future[Option[ClaimProof[I]]] =
    proverFromPassword(claim, password).flatMap {
      case None => Future.successful(None)
      case Some(prover) => prover.prove(replicaId).map(Some(_))
    }
  
  def proverFromPassword(claim: I, password: String)(using crypt: Crypt): Future[Option[ClaimProver[I]]] =
    val result = claimsBehindPassword.get(claim) match
      case None => Future.successful(None)
      case Some(claimBehindPassword) => 
        crypt.unwrapKey(claimBehindPassword, password)
    
    result.map(_.map(privateKey => ClaimProver[I](BinaryData(privateKey), claim)))

// We do not need any context or pipeline for this extension. It is just a state extension.
