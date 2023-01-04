package webapp.device.framework

import core.framework.*
import org.scalajs.*
import org.scalajs.dom
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.typedarray.*
import scala.util.*
import typings.std.global.TextEncoder
import scala.scalajs.js.JSON
import org.scalajs.dom.JsonWebKey

given Crypt with
  def generateKey: Future[CryptKeyValuePair] =
    dom.crypto.subtle.generateKey(
        new dom.EcKeyAlgorithm {
          val name = "ECDSA"
          val namedCurve = "P-256"
        },
        true,
        js.Array(dom.KeyUsage.sign, dom.KeyUsage.verify)
      )
      .toFuture
      .mapTo[dom.CryptoKeyPair]
      .flatMap(pair => 
        for
          privateKey <- exportPrivateKey(pair.privateKey)
          publicKey  <- exportPublicKey(pair.publicKey)
        yield CryptKeyValuePair(privateKey = privateKey, publicKey)
      )

  def wrapKey(privateKey: Array[Byte], password: String): Future[Array[Byte]] =
    for
      deriveKey <- dom.crypto.subtle.importKey(
          dom.KeyFormat.raw,
          new TextEncoder().encode(password).buffer,
          "PBKDF2",
          false,
          js.Array(dom.KeyUsage.deriveKey)
        )
        .toFuture
        .mapTo[dom.CryptoKey]
      
      aesKey <- dom.crypto.subtle.deriveKey(
          new dom.Pbkdf2Params {
            val name = "PBKDF2"
            val salt = new TextEncoder().encode("ratable_salt").buffer
            val iterations = 100000
            val hash = "SHA-512"
          },
          deriveKey,
          new dom.AesKeyAlgorithm {
            val name = "AES-KW"
            val length = 256
          },
          true,
          js.Array(dom.KeyUsage.encrypt, dom.KeyUsage.decrypt)
        )
        .toFuture
        .mapTo[dom.CryptoKey]
      
      keyImported <- importPrivateKey(privateKey)

      keyWraped <- dom.crypto.subtle.wrapKey(
          dom.KeyFormat.raw,
          keyImported,
          aesKey,
          "AES-KW"
        )
        .toFuture
        .mapTo[ArrayBuffer]
        .map(new Int8Array(_).toArray)      

    yield
      keyWraped

  def unwrapKey(key: Array[Byte], password: String): Future[Option[Array[Byte]]] =
    for
      deriveKey <- dom.crypto.subtle.importKey(
          dom.KeyFormat.raw,
          new TextEncoder().encode(password).buffer,
          "PBKDF2",
          false,
          js.Array(dom.KeyUsage.deriveKey)
        )
        .toFuture
        .mapTo[dom.CryptoKey]
      
      aesKey <- dom.crypto.subtle.deriveKey(
          new dom.Pbkdf2Params {
            val name = "PBKDF2"
            val salt = new TextEncoder().encode("ratable_salt").buffer
            val iterations = 100000
            val hash = "SHA-512"
          },
          deriveKey,
          new dom.AesKeyAlgorithm {
            val name = "AES-KW"
            val length = 256
          },
          true,
          js.Array(dom.KeyUsage.encrypt, dom.KeyUsage.decrypt)
        )
        .toFuture
        .mapTo[dom.CryptoKey]
      
      keyUnwraped <- dom.crypto.subtle.unwrapKey(
          dom.KeyFormat.raw,
          key.toTypedArray.buffer,
          aesKey,
          "AES-KW",
          new dom.EcKeyImportParams {
            val name = "ECDSA"
            val namedCurve = "P-256"
          },
          true,
          js.Array(dom.KeyUsage.sign, dom.KeyUsage.verify)
        )
        .toFuture
        .mapTo[dom.CryptoKey]
        .map(Some(_))
        .recover { case _ => None }

      keyExported <- keyUnwraped match
        case Some(key) => exportPrivateKey(key).map(Some(_))
        case None => Future.successful(None)
    yield
      keyExported

  def sign(key: Array[Byte], content: Array[Byte]): Future[Array[Byte]] =
    importPrivateKey(key).flatMap(cryptoKey =>
      dom.crypto.subtle.sign(
          new dom.EcdsaParams {
            val name = "ECDSA"
            val hash = "SHA-512"
          },
          cryptoKey,
          content.toTypedArray.buffer
        )
        .toFuture
        .mapTo[ArrayBuffer]
        .map(new Int8Array(_).toArray)
    )
    
  def verify(key: Array[Byte], content: Array[Byte], signature: Array[Byte]): Future[Boolean] =
    importPublicKey(key).flatMap(cryptoKey =>
      dom.crypto.subtle.verify(
          new dom.EcdsaParams {
            val name = "ECDSA"
            val hash = "SHA-512"
          },
          cryptoKey,
          signature.toTypedArray.buffer,
          content.toTypedArray.buffer
        )
        .toFuture
        .mapTo[Boolean]
    )

  private def importPrivateKey(key: Array[Byte]): Future[dom.CryptoKey] =
    dom.crypto.subtle.importKey(
        dom.KeyFormat.jwk,
        JSON.parse(new String(key)).asInstanceOf[JsonWebKey],
        new dom.EcKeyImportParams {
          val name = "ECDSA"
          val namedCurve = "P-256"
        },
        true,
        js.Array(dom.KeyUsage.sign, dom.KeyUsage.verify)
      )
      .toFuture
      .mapTo[dom.CryptoKey]

  private def exportPrivateKey(key: dom.CryptoKey): Future[Array[Byte]] =
    dom.crypto.subtle.exportKey(
        dom.KeyFormat.jwk,
        key
      )
      .toFuture
      .map(JSON.stringify(_).getBytes())

  private def importPublicKey(key: Array[Byte]): Future[dom.CryptoKey] =
    dom.crypto.subtle.importKey(
        dom.KeyFormat.raw,
        key.toTypedArray.buffer,
        new dom.EcKeyImportParams {
          val name = "ECDSA"
          val namedCurve = "P-256"
        },
        true,
        js.Array(dom.KeyUsage.sign, dom.KeyUsage.verify)
      )
      .toFuture
      .mapTo[dom.CryptoKey]

  private def exportPublicKey(key: dom.CryptoKey): Future[Array[Byte]] =
    dom.crypto.subtle.exportKey(
        dom.KeyFormat.raw,
        key
      )
      .toFuture
      .mapTo[ArrayBuffer]
      .map(new Int8Array(_).toArray)
      .andThen {
        case Success(value) => println(s"exported public key: ${value.length}")
        case Failure(exception) => println(s"exporting public key failed: $exception")
      }
