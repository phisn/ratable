package webapp.services

import kofre.base.*
import kofre.decompose.containers.DeltaBufferRDT
import kofre.dotted.Dotted
import kofre.syntax.DottedName
import loci.registry.{Binding, Registry}
import loci.transmitter.RemoteRef
import rescala.default.*
import scribe.Execution.global
import webapp.services.*
import webapp.store.LocalRatingState
import webapp.store.aggregates.*
import webapp.store.framework.*

import scala.concurrent.Future
import scala.reflect.Selectable.*
import scala.util.{Failure, Success}

class StateDistributionService(services: {
  val config: ApplicationConfig
  val stateProvider: StateProviderService
}):
  def registerRepository[Repository : Bottom : DecomposeLattice]: RepositoryRDT[Repository] =
    val rdt = DeltaBufferRDT[Repository](services.config.replicaID, Bottom[Repository].empty)

    val deltaEvt = Evt[DottedName[Repository]]()
    val actions = Evt[DeltaBufferRDT[Repository] => DeltaBufferRDT[Repository]]()

    val rdtSignal = Events.foldAll(rdt)(current =>
      Seq(
        deltaEvt.act(delta => current.resetDeltaBuffer().applyDelta(delta)),
        actions.act(mutator => mutator(current))
      )
    )

    RepositoryRDT[Repository](
      actions,
      rdtSignal.map(_.state.store)
    )

  private var observers = Map[RemoteRef, Disconnectable]()
  private var resendBuffer = Map[RemoteRef, Dotted[LocalRatingState]]()
  private val registry = new Registry

  private def distributeRDT[A](
      signal: Signal[DeltaBufferRDT[A]],
      deltaEvt: Evt[DottedName[A]]
  )(binding: Binding[Dotted[A] => Unit, Dotted[A] => Future[Unit]])(implicit
      dcl: DecomposeLattice[Dotted[A]],
      bottom: Bottom[Dotted[A]]
  ): Unit = {
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
  }
