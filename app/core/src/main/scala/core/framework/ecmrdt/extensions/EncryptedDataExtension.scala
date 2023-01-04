package core.framework.ecmrdt.extensions

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.framework.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

trait EncryptedDataExtension[D : JsonValueCodec](
  val data: BinaryData
):
  def decrypt(password: String)(using crypt: Crypt): Future[Option[D]] =
    crypt.decrypt(password, data.inner).map(_.map(readFromArray[D](_)))

object EncryptedData:
  def encrypt[D : JsonValueCodec](data: D, password: String)(using crypt: Crypt): Future[BinaryData] =
    crypt.encrypt(password, writeToArray(data)).map(BinaryData(_))
