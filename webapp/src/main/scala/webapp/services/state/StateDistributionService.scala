package webapp.services.state

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import concurrent.ExecutionContext.Implicits.global
import core.messages.*
import core.messages.client.*
import core.messages.delta_message.*
import core.messages.server.*
import core.store.framework.*
import org.scalajs.dom.WebSocket
import org.scalajs.dom.MessageEvent
import scala.collection.mutable.Map
import scala.reflect.Selectable.*
import scala.scalajs.js.typedarray.*
import scala.util.*
import sttp.client3.*
import sttp.client3.jsoniter.*
import rescala.default.*
import webapp.services.*

trait StateDistributionServiceInterface:
  def aggregateEventsFor[A : JsonValueCodec](id: String): (Event[A], Evt[Tag])
  def pushDelta[A : JsonValueCodec](id: String, delta: TaggedDelta[A]): Unit

class StateDistributionService(services: {
  val config: ApplicationConfigInterface
}) extends StateDistributionServiceInterface:
  private val eventRouter = Map[String, EventRouterEntry]()
  private val pushDeltaEvent = Evt[DeltaMessage]()

  case class EventRouterEntry(
    deltaEvent: Evt[String],
    deltaAckEvent: Evt[Tag]
  )

  def aggregateEventsFor[A : JsonValueCodec](id: String): (Event[A], Evt[Tag]) =
    val entry = EventRouterEntry(
      deltaEvent = Evt[String](),
      deltaAckEvent = Evt[Tag]()
    )

    eventRouter(id) = entry

    entry.deltaEvent
      .map(d => s"Received message")
      .observe(println)
    (
      entry.deltaEvent.map(readFromString[A](_)),
      entry.deltaAckEvent
    )

  def pushDelta[A : JsonValueCodec](id: String, delta: TaggedDelta[A]) =
    pushDeltaEvent.fire(DeltaMessage(id, writeToString(delta)))

  private def handleWebsocketConnection(ws: WebSocket) =
    ws.onmessage = event => handleWebsocketMessage(event)

    pushDeltaEvent.observe { message =>
      val clientMessage = ClientMessage(
        ClientMessage.Message.DeltaMessage(message)
      )

      val clientMessageEncoded = clientMessage.toByteArray.toTypedArray.buffer
      ws.send(clientMessageEncoded)

      println(s"Send delta message: L${clientMessageEncoded.byteLength}")
    }

  private def handleWebsocketMessage(event: MessageEvent): Unit =
    ServerMessage.validate(
      new Int8Array(event.data.asInstanceOf[ArrayBuffer]).toArray
    ) match
      case Success(value) => 
        handleServerMessage(value)
        
      case Failure(exception) => 
        println(s"Could not parse server message: $exception")
        exception.printStackTrace()

  private def handleServerMessage(value: ServerMessage): Unit =
    value.message match
      case ServerMessage.Message.DeltaMessage(message) =>
        eventRouter(message.aggregateId).deltaEvent.fire(message.deltaJson)
      
      case ServerMessage.Message.AcknowledgeDeltaMessage(message) =>
        eventRouter(message.aggregateId).deltaAckEvent.fire(message.tag)

      case _ =>
        println(s"Unknown message: $value")

  try {
    val backend = FetchBackend()

    val request = basicRequest.get(uri"${services.config.backendUrl}login?userid=${services.config.replicaID}")
      .response(asJson[WebPubSubConnectionMessage])
      .send(backend)
      .map(_.body match
        case Left(error) => throw error
        case Right(value) => value
      )
      .map(message => new WebSocket(message.url))
      .foreach(handleWebsocketConnection)
  }
  catch {
    case cause: Throwable =>
      println(s"Could not connect to backend: $cause")
      cause.printStackTrace()
  }

    
    /*
      .map(_.)
      .foreach {
        case Right(delta) => deltaEvt.fire(delta.delta)
        case Left(error) => println(s"Error fetching aggregate $id: $error")
      }
    */

    // actions.map(_(changes.now)).observe(pushDelta)

    


    /*
    val rdt = DeltaBufferRDT[A](services.config.replicaID)

    val deltaEvt = Evt[DottedName[A]]()
    val deltaPushedEvt = Evt[Unit]()

    val actions = Evt[A => A]()

    // current state consists only of all actions by usecases and deltas from other replicas
    val rdtSignal = Events.foldAll(rdt)(current =>
      Seq(
        deltaPushedEvt.act(_ => current.resetDeltaBuffer()),
        deltaEvt.act(current.applyDelta(_)),
        actions.act(mutator => current.applyDelta(DottedName(current.replicaID, Dotted(mutator(current.state.store)))))
      )
    )

    rdtSignal.observe { rdt =>
      val deltaStateList = rdt.deltaBuffer.collect {
        case DottedName(replicaID, deltaState) if replicaID == services.config.replicaID => deltaState
      }

      val combinedState = deltaStateList.reduceOption(DecomposeLattice[Dotted[A]].merge)
      combinedState.foreach { state =>
        pushDelta(state)
      }
    }

    // pack all into a single facade to expose minimal interface
    Facade[A](
      actions,
      rdStignal.map(_.state.store)
    )
    */

    /*
    registry.bindSbj(binding) { (remoteRef: RemoteRef, deltaState: Dotted[A]) =>
      deltaEvt.fire(DottedName(remoteRef.toString, deltaState))
    }

    var observers    = Map[RemoteRef, Disconnectable]()
    var resendBuffer = Map[RemoteRef, Dotted[A]]()

    def registerRemote(remoteRef: RemoteRef): Unit = {
      val remoteUpdate: Dotted[A] => Future[Unit] = registry.lookup(binding, remoteRef)

      // Send full state to initialize remote
      val currentState = signal.readValueOnce.state
      if (currentState != bottom.empty) remoteUpdate(currentState)

      // Whenever the crdt is changed propagate the delta
      // Praktisch wäre etwas wie crdt.observeDelta
      val observer = signal.observe { s =>
        val deltaStateList = s.deltaBuffer.collect {
          case DottedName(replicaID, deltaState) if replicaID != remoteRef.toString => deltaState
        } ++ resendBuffer.get(remoteRef).toList

        val combinedState = deltaStateList.reduceOption(DecomposeLattice[Dotted[A]].merge)

        combinedState.foreach { s =>
          val mergedResendBuffer = resendBuffer.updatedWith(remoteRef) {
            case None       => Some(s)
            case Some(prev) => Some(DecomposeLattice[Dotted[A]].merge(prev, s))
          }

          if (remoteRef.connected) {
            remoteUpdate(s).onComplete {
              case Success(_) =>
                resendBuffer = resendBuffer.removed(remoteRef)
              case Failure(_) =>
                resendBuffer = mergedResendBuffer
            }
          } else {
            resendBuffer = mergedResendBuffer
          }
        }
      }

      observers += (remoteRef -> observer)
    }
        
    registry.remoteJoined.monitor(registerRemote)
    registry.remotes.foreach(registerRemote)
    registry.remoteLeft.monitor(observers(_).disconnect())
    */
