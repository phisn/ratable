package core.framework.ecmrdt.extensions

import core.framework.*
import core.framework.ecmrdt.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

// The idea behind this extension is simple. We are already using the asymmetric cryptography extension
// with claims. We now encrypt each claim prover with a password. This way, each user with access to the
// password can prove their claim and do specific actions.

trait ClaimByPasswordStateExtension[I]:
  def claimsBehindPassword: Map[I, Array[Byte]]

  def proofFromPassword(replicaId: ReplicaId, claim: I, password: String)(using crypt: Crypt) =
    proverFromPassword(claim, password).map(_.map(_.prove(replicaId)))
  
  def proverFromPassword(claim: I, password: String)(using crypt: Crypt) =
    val result = claimsBehindPassword.get(claim) match
      case None => Future.successful(None)
      case Some(claimBehindPassword) => 
        crypt.unwrapKey(claimBehindPassword, password)
    
    result.map(_.map(privateKey => new ClaimProver[I](privateKey, claim)))

// We do not need any context or pipeline for this extension. It is just a state extension.

