package core.domain.aggregates.ratable

import cats.data.*
import cats.implicits.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.framework.*
import core.framework.ecmrdt.*
import core.framework.ecmrdt.extensions.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random
import com.github.plokhotnyuk.jsoniter_scala.core.JsonKeyCodec

case class Category(
  val title: String
)

case class Rating(
  val ratingForCategory: Map[Int, Int]
)

enum RatableClaims extends Enum[RatableClaims]:
  case CanRate

case class Ratable(
  val claims: List[Claim[RatableClaims]],
  val claimsBehindPassword: Map[RatableClaims, BinaryDataWithIV],

  val title: String,
  val categories: Map[Int, Category],
  val ratings: Map[ReplicaId, Rating]
)
extends AsymPermissionStateExtension[RatableClaims], ClaimByPasswordStateExtension[RatableClaims]:
  def rate(replicaId: ReplicaId, ratingForCategory: Map[Int, Int]): Ratable =
    copy(
      ratings = ratings + (replicaId -> Rating(ratingForCategory))
    )

  def categoriesWithRating: Map[Int, (Category, Int)] =
    if ratings.size == 0 then
      return Map()

    categories
      .map((index, category) =>
        (index, (
          category,
          ratings
            .map(_._2.ratingForCategory.getOrElse(index, 0))
            .sum / ratings.size
        ))
      )
      .toMap

object Ratable:
  given (using Crypt): EffectPipeline[Ratable, RatableContext] = EffectPipeline(
    AsymPermissionEffectPipeline[Ratable, RatableClaims, RatableContext]
  )

  given JsonValueCodec[Ratable] = JsonCodecMaker.make
  
  given InitialECmRDT[Ratable] = InitialECmRDT(Ratable(
    List(), Map(), "missing-title", Map(0 -> Category("missing-categories")), Map()
  ))

case class RatableContext(
  val replicaId: ReplicaId,
  val proofs: List[ClaimProof[RatableClaims]]
) 
extends IdentityContext 
   with AsymPermissionContextExtension[RatableClaims]

object RatableContext:
  given JsonValueCodec[RatableContext] = JsonCodecMaker.make

sealed trait RatableEvent extends Event[Ratable, RatableContext]

object RatableEvent:
  given JsonValueCodec[RatableEvent] = JsonCodecMaker.make

case class RateEvent(
  val ratingForCategory: Map[Int, Int]
) extends RatableEvent:
  def asEffect: Effect[Ratable, RatableContext] =
    (state, context, meta) =>
      for
        _ <- context.verifyClaim(RatableClaims.CanRate)
        _ <- EitherT.cond(ratingForCategory.size == state.categories.size, (),
          RatableError(s"Rating must contain ${state.categories.size} categories, but ${ratingForCategory.size} given"))
      yield
        state.rate(context.replicaId, ratingForCategory)


case class CreateRatableEvent(
  val canRate: Claim[RatableClaims],
  val canRateBehindPassword: BinaryDataWithIV,

  val title: String,
  val categories: List[String]  
) extends RatableEvent:
  def asEffect: Effect[Ratable, RatableContext] =
    (state, context, meta) =>
      for
        _ <- EitherT.cond(meta.ownerReplicaId == context.replicaId, (),
          RatableError(s"Replica ${context.replicaId} is not the owner ${meta.ownerReplicaId} of this object."))
        
      yield
        Ratable(
          List(canRate),
          Map(RatableClaims.CanRate -> canRateBehindPassword),
          title,
          categories
            .zipWithIndex
            .map((title, index) => (index, Category(title)))
            .toMap,
          Map()
        )

case class CreateRatableResult(
  val event: CreateRatableEvent,
  val password: String
)

def createRatable(title: String, categories: List[String])(using crypt: Crypt): Future[CreateRatableResult] =
  val password = Random.alphanumeric.take(18).mkString

  for
    (canRateClaim, canRateProver) <- Claim.create(RatableClaims.CanRate)
    claimBehindPassword <- ClaimBehindPassword(canRateProver.privateKey.inner, password)

  yield
    CreateRatableResult(
      CreateRatableEvent(
        canRateClaim,
        claimBehindPassword,
        title,
        categories
      ),
      password
    )
