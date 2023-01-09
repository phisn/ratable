package webapp.device.framework

import cats.data.*
import cats.implicits.*
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
import org.scalajs.dom.BufferSource

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

  def wrapKey(privateKey: Array[Byte], password: String): Future[BinaryDataWithIV] =
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
            val name = "AES-GCM"
            val length = 256
          },
          true,
          js.Array(dom.KeyUsage.wrapKey, dom.KeyUsage.unwrapKey)
        )
        .toFuture
        .mapTo[dom.CryptoKey]

      keyImported <- importPrivateKey(privateKey)

      newIv = dom.crypto.getRandomValues(new Uint8Array(12)).buffer

      keyWraped <- dom.crypto.subtle.wrapKey(
          dom.KeyFormat.jwk,
          keyImported,
          aesKey,
          new dom.KeyAlgorithm {
            val name = "AES-GCM"
            val iv = newIv
          }
        )
        .toFuture
        .mapTo[ArrayBuffer]
        .map(new Int8Array(_).toArray)      

    yield
      BinaryDataWithIV(keyWraped, new Int8Array(newIv).toArray)

  def unwrapKey(keyWraped: BinaryDataWithIV, password: String): EitherT[Future, RatableError, Array[Byte]] =
    for
      deriveKey <- EitherT.liftF(
        dom.crypto.subtle.importKey(
          dom.KeyFormat.raw,
          new TextEncoder().encode(password).buffer,
          "PBKDF2",
          false,
          js.Array(dom.KeyUsage.deriveKey)
        )
        .toFuture
        .mapTo[dom.CryptoKey]
      )
      
      aesKey <- EitherT.liftF(
        dom.crypto.subtle.deriveKey(
          new dom.Pbkdf2Params {
            val name = "PBKDF2"
            val salt = new TextEncoder().encode("ratable_salt").buffer
            val iterations = 100000
            val hash = "SHA-512"
          },
          deriveKey,
          new dom.AesKeyAlgorithm {
            val name = "AES-GCM"
            val length = 256
          },
          true,
          js.Array(dom.KeyUsage.wrapKey, dom.KeyUsage.unwrapKey)
        )
        .toFuture
        .mapTo[dom.CryptoKey]
      )
      
      keyUnwraped <- EitherT(
        dom.crypto.subtle.unwrapKey(
          dom.KeyFormat.jwk,
          keyWraped.key.inner.toTypedArray.buffer,
          aesKey,
          new dom.KeyAlgorithm {
            val name = "AES-GCM"
            val iv = new Int8Array(keyWraped.iv.inner.toTypedArray).buffer
          },
          new dom.EcKeyImportParams {
            val name = "ECDSA"
            val namedCurve = "P-256"
          },
          true,
          js.Array(dom.KeyUsage.sign)
        )
        .toFuture
        .mapTo[dom.CryptoKey]
        .map(Right(_))
        .recover { case e => RatableError(s"Unwrapkey failed because of '${e.getMessage}'").asLeft }
      )

      keyExported <- EitherT.liftF(
        exportPrivateKey(keyUnwraped)
      )
    yield
      keyExported

  def encrypt(password: String, content: Array[Byte]): Future[BinaryDataWithIV] =
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

      newIv = dom.crypto.getRandomValues(new Uint8Array(12)).buffer
      
      aesKey <- dom.crypto.subtle.deriveKey(
          new dom.Pbkdf2Params {
            val name = "PBKDF2"
            val salt = new TextEncoder().encode("ratable_salt").buffer
            val iterations = 100000
            val hash = "SHA-512"
          },
          deriveKey,
          new dom.AesKeyAlgorithm {
            val name = "AES-GCM"
            val length = 256
          },
          true,
          js.Array(dom.KeyUsage.encrypt, dom.KeyUsage.decrypt)
        )
        .toFuture
        .mapTo[dom.CryptoKey]
      
      encrypted <- dom.crypto.subtle.encrypt(
          new dom.AesGcmParams {
            val name = "AES-GCM"
            val iv = new TextEncoder().encode("ratable_iv").buffer
            val additionalData = null
            val tagLength = 128
          },
          aesKey,
          content.toTypedArray.buffer
        )
        .toFuture
        .mapTo[ArrayBuffer]
        .map(new Int8Array(_).toArray)
    yield
      BinaryDataWithIV(encrypted, new Int8Array(newIv).toArray)

  def decrypt(password: String, content: BinaryDataWithIV): EitherT[Future, RatableError, Array[Byte]] =
    for
      deriveKey <- EitherT.liftF(
        dom.crypto.subtle.importKey(
          dom.KeyFormat.raw,
          new TextEncoder().encode(password).buffer,
          "PBKDF2",
          false,
          js.Array(dom.KeyUsage.deriveKey)
        )
        .toFuture
        .mapTo[dom.CryptoKey]
      )
      
      aesKey <- EitherT.liftF(
        dom.crypto.subtle.deriveKey(
          new dom.Pbkdf2Params {
            val name = "PBKDF2"
            val salt = new TextEncoder().encode("ratable_salt").buffer
            val iterations = 100000
            val hash = "SHA-512"
          },
          deriveKey,
          new dom.AesKeyAlgorithm {
            val name = "AES-GCM"
            val length = 256
          },
          true,
          js.Array(dom.KeyUsage.encrypt, dom.KeyUsage.decrypt)
        )
        .toFuture
        .mapTo[dom.CryptoKey]
      )
      
      decrypted <- EitherT(dom.crypto.subtle.decrypt(
          new dom.AesGcmParams {
            val name = "AES-GCM"
            val iv = content.iv.inner.toTypedArray.buffer
            val additionalData = null
            val tagLength = 128
          },
          aesKey,
          content.key.inner.toTypedArray.buffer
        )
        .toFuture
        .mapTo[ArrayBuffer]
        .map(new Int8Array(_).toArray)
        .map(Right(_))
        .recover { case e => RatableError("Decrypt failed because of '${e.getMessage}'").asLeft }
      )
    yield
      decrypted

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
        js.Array(dom.KeyUsage.sign)
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
        js.Array(dom.KeyUsage.verify)
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
