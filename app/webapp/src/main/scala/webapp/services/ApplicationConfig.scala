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

trait ApplicationConfigInterface:
  def backendUrl: String
  
  def darkMode: Var[Boolean]
  def language: Var[String]

  def websocketReconnectInterval: Int
  def replicaID: String

class ApplicationConfig(services: {}) extends ApplicationConfigInterface:
  // Should use a config library but was unable to get it working with scalajs :(
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

  def replicaID: String = 
    // Hack. Later to be exported into state
    var item = window.localStorage.getItem("replicaID")

    if item == null then
      item = ThreadLocalRandom.current().nextLong().toHexString
      window.localStorage.setItem("replicaID", item)

    item
