package core.framework

import cats.data.*
import cats.implicits.*
import scala.concurrent.*

case class CryptKeyValuePair(
  val privateKey: Array[Byte],
  val publicKey: Array[Byte]
)

case class BinaryDataWithIV(
  val key: BinaryData,
  val iv: BinaryData
)

object BinaryDataWithIV:
  def apply(key: Array[Byte], iv: Array[Byte]): BinaryDataWithIV =
    BinaryDataWithIV(BinaryData(key), BinaryData(iv))

trait Crypt:
  def generateKey: Future[CryptKeyValuePair]

  def encrypt(password: String, content: Array[Byte]): Future[BinaryDataWithIV]
  def decrypt(password: String, content: BinaryDataWithIV): OptionT[Future, Array[Byte]]

  def wrapKey(key: Array[Byte], password: String): Future[BinaryDataWithIV]
  def unwrapKey(key: BinaryDataWithIV, password: String): OptionT[Future, Array[Byte]]

  def sign(key: Array[Byte], content: Array[Byte]): Future[Array[Byte]]
  def verify(key: Array[Byte], content: Array[Byte], signature: Array[Byte]): Future[Boolean]
  
  def sign(key: Array[Byte], content: String): Future[Array[Byte]] =
    sign(key, content.getBytes())

  def verify(key: Array[Byte], content: String, signature: Array[Byte]): Future[Boolean] =
    verify(key, content.getBytes(), signature)
