package webapp.mocks

import rescala.default.*
import webapp.services.*

class ApplicationConfigMock(private var _replicaID: String = "mockReplicaID") extends ApplicationConfigInterface:
  def backendUrl = "http://localhost:8080"
  def darkMode = Var(false)
  def replicaID = _replicaID

  def withReplicaID(replicaID: String)(f: => Unit) =
    val oldReplicaID = _replicaID
    _replicaID = replicaID
    f
    _replicaID = oldReplicaID
    replicaID
