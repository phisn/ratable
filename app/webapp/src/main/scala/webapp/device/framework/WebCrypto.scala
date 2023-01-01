package webapp.device.framework

import core.framework.*
import org.scalajs.*
import org.scalajs.dom
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.typedarray.*
import typings.std.global.TextEncoder

given Crypt with
  def generateKey: Future[CryptKeyValuePair] =
    dom.crypto.subtle.generateKey(
        new dom.RsaHashedKeyGenParams {
          val name = "RSASSA-PKCS1-v1_5"
          val modulusLength = 4096
          val publicExponent = Uint8Array.of(1, 0, 1)
          val hash = "SHA-256"
        },
        true,
        js.Array(dom.KeyUsage.sign, dom.KeyUsage.verify)
      )
      .toFuture
      .mapTo[dom.CryptoKeyPair]
      .flatMap(pair => 
        for
          privateKey <- exportKey(pair.privateKey)
          publicKey  <- exportKey(pair.publicKey)
        yield CryptKeyValuePair(privateKey = privateKey, publicKey)
      )

  def sign(key: Array[Byte], content: Array[Byte]): Future[Array[Byte]] =
    importKey(key).flatMap(cryptoKey =>
      dom.crypto.subtle.sign(
          "RSASSA-PKCS1-v1_5",
          cryptoKey,
          content.toTypedArray.buffer
        )
        .toFuture
        .mapTo[ArrayBuffer]
        .map(new Int8Array(_).toArray)
    )
    
  def verify(key: Array[Byte], content: Array[Byte], signature: Array[Byte]): Future[Boolean] =
    importKey(key).flatMap(cryptoKey =>
      dom.crypto.subtle.verify(
          "RSASSA-PKCS1-v1_5",
          cryptoKey,
          signature.toTypedArray.buffer,
          content.toTypedArray.buffer
        )
        .toFuture
        .mapTo[Boolean]
    )

  private def importKey(key: Array[Byte]): Future[dom.CryptoKey] =
    dom.crypto.subtle.importKey(
        dom.KeyFormat.raw,
        key.toTypedArray.buffer,
        new dom.RsaHashedImportParams {
          val name = "RSASSA-PKCS1-v1_5"
          val hash = "SHA-256"
        },
        true,
        js.Array(dom.KeyUsage.sign, dom.KeyUsage.verify)
      )
      .toFuture
      .mapTo[dom.CryptoKey]

  private def exportKey(key: dom.CryptoKey): Future[Array[Byte]] =
    dom.crypto.subtle.exportKey(
        dom.KeyFormat.raw,
        key
      )
      .toFuture
      .mapTo[ArrayBuffer]
      .map(new Int8Array(_).toArray)
