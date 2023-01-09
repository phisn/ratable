package core.domain.aggregates.library

import cats.data.*
import cats.implicits.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.framework.*
import core.framework.ecmrdt.*
import core.framework.ecmrdt.extensions.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

case class RatableEntry(
  val password: Option[String]
)

case class RatableLibrary(
  val replicaId: ReplicaId,
  val entries: Map[AggregateId, RatableEntry]

) extends SingleOwnerStateExtension

object RatableLibrary:
  given EffectPipeline[RatableLibrary, RatableLibraryContext] = EffectPipeline(
    SingleOwnerEffectPipeline()
  )

  given JsonValueCodec[RatableLibrary] = JsonCodecMaker.make

case class RatableLibraryContext(
  val replicaId: ReplicaId
) extends IdentityContext

object RatableLibraryContext:
  given JsonValueCodec[RatableLibraryContext] = JsonCodecMaker.make

sealed trait RatableLibraryEvent extends Event[RatableLibrary, RatableLibraryContext]

object RatableLibraryEvent:
  given JsonValueCodec[RatableLibraryEvent] = JsonCodecMaker.make

case class IndexRatableEvent(
  val id: AggregateId,
  val password: Option[String]
) extends RatableLibraryEvent:
  def asEffect: Effect[RatableLibrary, RatableLibraryContext] =
    (state, context) => EitherT.pure(
      state.copy(entries = state.entries + (id -> RatableEntry(password)))
    )

case class UnindexRatableEvent(
  val id: AggregateId
) extends RatableLibraryEvent:
  def asEffect: Effect[RatableLibrary, RatableLibraryContext] =
    (state, context) => EitherT.pure(
      state.copy(entries = state.entries - id)
    )
