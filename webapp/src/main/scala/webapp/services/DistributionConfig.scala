package webapp.services

import org.scalajs.dom

import java.util.concurrent.ThreadLocalRandom
import scala.scalajs.js

class DistributionConfig:
  val replicaId: String = ThreadLocalRandom.current().nextLong().toHexString

  val rtcConfig = new dom.RTCConfiguration {
    iceServers = js.Array[dom.RTCIceServer](
      new dom.RTCIceServer {
        urls = js.Array("stun:openrelay.metered.ca:80")
      },
      new dom.RTCIceServer {
        urls = js.Array("turn:openrelay.metered.ca:80")
        username = "openrelayproject"
        credential = "openrelayproject"
      },
      new dom.RTCIceServer {
        urls = js.Array("turn:openrelay.metered.ca:443")
        username = "openrelayproject"
        credential = "openrelayproject"
      },
      new dom.RTCIceServer {
        urls = js.Array("turn:openrelay.metered.ca:443?transport=tcp")
        username = "openrelayproject"
        credential = "openrelayproject"
      }
    )
  }
