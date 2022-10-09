package webapp.services

import org.scalajs.dom

import java.util.concurrent.ThreadLocalRandom
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scalajs.js.Thenable.Implicits.thenable2future

import java.nio.file.{Files, Paths}
import scala.concurrent.*
import scala.concurrent.Await
import scala.concurrent.duration._
import org.scalajs.dom.*

import rescala.default._

class ApplicationConfig:
  val darkMode = Var(false)

  // should use a config library but was unable to get it working with scalajs
  var backendUrl = if dom.window.location.hostname.contains("localhost") then
    "http://localhost:7071/api/"
  else
    "https://func-localrating-backend.azurewebsites.net/api/"

  val replicaID: String = ThreadLocalRandom.current().nextLong().toHexString
