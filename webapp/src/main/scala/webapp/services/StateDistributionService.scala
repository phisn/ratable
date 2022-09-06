package webapp.services

import kofre.base.*
import kofre.datatypes.GrowOnlyCounter
import kofre.dotted.Dotted
import kofre.syntax.DottedName
import loci.registry.{Binding, Registry}
import loci.serializer.jsoniterScala.*
import loci.transmitter.RemoteRef
import rescala.default.*
import webapp.store.codec.*
import webapp.store.LocalRatingState

import java.util.concurrent.ThreadLocalRandom
import reflect.Selectable.*
import scala.concurrent.Future
import scala.util.{Failure, Success}

class StateDistributionService(services: {
  val stateProvider: StateProviderService
}):
  private var observers = Map[RemoteRef, Disconnectable]()
  private var resendBuffer = Map[RemoteRef, Dotted[LocalRatingState]]()
  private val registry = new Registry

  private def registerRemote(remoteRef: RemoteRef)(implicit bottom: Bottom[Dotted[LocalRatingState]]): Unit =
    val update: Dotted[LocalRatingState] => Future[Unit] = registry.lookup(services.stateProvider.stateBinding, remoteRef)

    val currentState = services.stateProvider.state.readValueOnce.state
    if (currentState != bottom.empty) update(currentState)

    val observer = services.stateProvider.state.observe { s =>
      val deltaStateList = s.deltaBuffer.collect {
        case DottedName(replicaID, deltaState) if replicaID != remoteRef.toString => deltaState
      } ++ resendBuffer.get(remoteRef).toList

      val combinedState = deltaStateList.reduceOption(DecomposeLattice[Dotted[LocalRatingState]].merge)

      combinedState.foreach { s =>
        val mergedResendBuffer = resendBuffer.updatedWith(remoteRef) {
          case None => Some(s)
          case Some(prev) => Some(DecomposeLattice[Dotted[LocalRatingState]].merge(prev, s))
        }

        if (remoteRef.connected) {
          update(s).onComplete {
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

    registry.bindSbj(services.stateProvider.stateBinding) { (remoteRef: RemoteRef, deltaState: Dotted[GrowOnlyCounter]) =>
      services.stateProvider.deltaDispatcher.fire(DottedName(remoteRef.toString, deltaState))
    }
