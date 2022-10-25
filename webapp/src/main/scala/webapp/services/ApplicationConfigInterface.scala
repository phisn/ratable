package webapp.services

import rescala.default.*

trait ApplicationConfigInterface:
  def backendUrl: String
  def darkMode: Var[Boolean]
  def replicaID: String
