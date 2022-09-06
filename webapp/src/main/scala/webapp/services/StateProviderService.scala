package webapp.services

import kofre.decompose.containers.DeltaBufferRDT
import kofre.dotted.Dotted
import kofre.syntax.DottedName
import loci.registry.Binding
import loci.serializer.jsoniterScala.*
import rescala.default.*
import webapp.services.*
import webapp.store.LocalRatingState

import reflect.Selectable.*
import scala.concurrent.Future

class StateProviderService(services: {
  val config: DistributionConfig
}):
  val state: Signal[DeltaBufferRDT[LocalRatingState]]
  val stateBinding: Binding[Dotted[LocalRatingState] => Unit, Dotted[LocalRatingState] => Future[Unit]]

  val deltaDispatcher = Evt[DottedName[LocalRatingState]]()
  val useCaseDispatcher = Evt[DeltaBufferRDT[LocalRatingState] => DeltaBufferRDT[LocalRatingState]]()

  Events.foldAll(DeltaBufferRDT[LocalRatingState](services.config.replicaId, LocalRatingState()))(current =>
    Seq(
      useCaseDispatcher.act(useCase => useCase(current)),
      deltaDispatcher.act(delta => current.resetDeltaBuffer().applyDelta(delta))
    )
  )
