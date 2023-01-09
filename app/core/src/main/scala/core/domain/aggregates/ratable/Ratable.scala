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
  def apply(title: String, categories: List[String])(using crypt: Crypt): Future[(Ratable, String)] =
    val password = Random.alphanumeric.take(18).mkString

    for
      (canRateClaim, canRateProver) <- Claim.create(RatableClaims.CanRate)
      claimBehindPassword <- ClaimBehindPassword(canRateProver.privateKey.inner, password)

      ratable = Ratable(
        List(canRateClaim),
        Map(RatableClaims.CanRate -> claimBehindPassword),
        title,
        categories
          .zipWithIndex
          .map((title, index) => (index, Category(title)))
          .toMap,
        Map()
      )

    yield
      (ratable, password)

  given (using Crypt): EffectPipeline[Ratable, RatableContext] = EffectPipeline(
    AsymPermissionEffectPipeline[Ratable, RatableClaims, RatableContext]
  )

  given JsonValueCodec[Ratable] = JsonCodecMaker.make

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
    (state, context) =>
      for
        _ <- context.verifyClaim(RatableClaims.CanRate)
        _ <- EitherT.cond(ratingForCategory.size == state.categories.size, (),
          RatableError(s"Rating must contain ${state.categories.size} categories, but ${ratingForCategory.size} given"))
      yield
        state.rate(context.replicaId, ratingForCategory)

/*
extension (ratable: Ratable)
  def rateEvent(ratingForCategory: Map[Int, Int])(using crypt: Crypt): RateEvent =
    EventWithContext(

    )
*/
