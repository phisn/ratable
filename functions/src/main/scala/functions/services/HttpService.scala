package functions.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import functions.*
import scala.reflect.Selectable.*
import scala.scalajs.js

trait HttpServiceInterface:
  def query(key: String): String
  def body[A : JsonValueCodec]: A
  def respond[A : JsonValueCodec](statusCode: Int, body: A): Unit

class HttpService(
  services: Services,
  context: js.Dynamic
) extends HttpServiceInterface:
  def query(key: String): String =
    context.req.query.asInstanceOf[js.Dictionary[String]].get(key).get

  def body[A : JsonValueCodec] = 
    readFromString(context.req.rawBody.asInstanceOf[String])

  def respond[A : JsonValueCodec](
    statusCode: Int,
    body: A
  ) = 
    respondWithJson(statusCode, writeToString(body))

  def respondWithJson(
    statusCode: Int,
    body: String
  ) =
    context.res = js.Dynamic.literal(
      "status" -> statusCode,
      "body" -> body
    )

    context.done()
