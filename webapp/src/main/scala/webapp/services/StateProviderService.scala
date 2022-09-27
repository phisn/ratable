package webapp.services

import kofre.decompose.containers.DeltaBufferRDT
import kofre.dotted.Dotted
import kofre.syntax.DottedName
import loci.registry.Binding
import loci.serializer.jsoniterScala.*
import org.w3c.dom.css.Counter
import rescala.default.*
import webapp.services.*
import webapp.store.aggregates
import webapp.store.LocalRatingState

import reflect.Selectable.*
import scala.concurrent.Future

class StateProviderService(services: {
  val config: DistributionConfig
}):
  val state: Signal[DeltaBufferRDT[LocalRatingState]]
  val stateBinding: Binding[Dotted[LocalRatingState] => Unit, Dotted[LocalRatingState] => Future[Unit]]

  val deltaDispatcher = Evt[DottedName[LocalRatingState]]()
  val counterUseCaseDispatcher = Evt[Counter => Counter]()

  Events.foldAll(
    LocalRatingState(
      DeltaBufferRDT[Counter](services.config.replicaId, Counter()),
    ))(current =>
    Seq(
      counterUseCaseDispatcher.act(useCase => LocalRatingState(current.counter.applyDelta(useCase(useCase)))),
      deltaDispatcher.act(delta => current.resetDeltaBuffer().applyDelta(delta))
    )
  )
