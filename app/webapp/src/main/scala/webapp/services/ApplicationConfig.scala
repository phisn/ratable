package webapp.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.*
import java.nio.file.{Files, Paths}
import java.util.concurrent.ThreadLocalRandom
import org.scalajs.dom
import org.scalajs.dom.*
import rescala.default._
import scala.scalajs.js
import scala.concurrent.*
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import scala.util.*
import scalajs.js.Thenable.Implicits.thenable2future

trait ApplicationConfigInterface:
  def backendUrl: String
  
  def darkMode: Var[Boolean]
  def language: Var[String]

  def websocketReconnectInterval: Int
  def replicaId(using Crypt): Future[PrivateReplicaId]

class ApplicationConfig(services: {
  val logger: LoggerServiceInterface
}) extends ApplicationConfigInterface:
  // Should use a config library but was unable to get it working with scalajs :(
  // TODO: Use webpack plugin
  def backendUrl = if dom.window.location.hostname.contains("localhost") then
    "http://localhost:7071/api/"
  else
    "https://api.ratable.org/api/"

  val darkModeVar = Var {
    val darkModePreference = dom.window.localStorage.getItem("darkMode")

    if darkModePreference != null then
      darkModePreference == "true"
    else
      dom.window.matchMedia("(prefers-color-scheme: dark)").matches
  }

  darkModeVar.changed.observe(mode =>
    dom.window.localStorage.setItem("darkMode", mode.toString)
  )

  def darkMode = darkModeVar
  
  val languageVar = Var {
    if window.localStorage.getItem("language") != null then
      window.localStorage.getItem("language")
    else
      window.navigator.language
  }

  languageVar.changed.observe(lang =>
    window.localStorage.setItem("language", lang)
  )

  def language: Var[String] =
    languageVar

  def websocketReconnectInterval = 
    30.seconds.toMillis.toInt

  def replicaId(using Crypt): Future[PrivateReplicaId] =
    val item = window.localStorage.getItem("replicaId")

    if item != null then
      try
        return Future.successful(readFromString[PrivateReplicaId](item))
      catch
        case e: Exception =>
          services.logger.error("Failed to read replicaId from local storage")
          window.localStorage.removeItem("replicaId")

    for
      replicaId <- PrivateReplicaId()
    yield
      window.localStorage.setItem("replicaId", writeToString(replicaId))
      replicaId
