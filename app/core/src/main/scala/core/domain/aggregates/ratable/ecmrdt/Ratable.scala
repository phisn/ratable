package core.domain.aggregates.ratable.ecmrdt

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.framework.*
import core.framework.ecmrdt.*
import core.framework.ecmrdt.extensions.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

case class Category(
  val title: String
)

case class Rating(
  val ratingForCategory: Map[Int, Int]
)

enum RatableClaims:
  case CanRate

case class Ratable(
  val claims: Set[Claim[RatableClaims]],
//  val claimsBehindPassword: Map[I, EncryptedClaimProver],


  val title: String,
  val categories: Map[Int, Category],
  val ratings: Map[ReplicaId, Rating]
)
extends AsymPermissionStateExtension[RatableClaims]: //, ClaimByPasswordStateExtension[RatableClaims]:
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
  def apply(claims: Set[Claim[RatableClaims]], title: String, categories: List[String]): Ratable =
    Ratable(
      claims,
      title,
      categories
        .zipWithIndex
        .map((title, index) => (index, Category(title)))
        .toMap,
      Map()
    )

  given (using Crypt): EffectPipeline[Ratable, RatableContext] = EffectPipeline(
    AsymPermissionEffectPipeline[Ratable, RatableClaims, RatableContext]
  )

  given JsonValueCodec[Ratable] = JsonCodecMaker.make

case class RatableContext(
  val replicaId: ReplicaId,
  val proofs: Set[ClaimProof[RatableClaims]]
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
    Effect.from(
      (state, context) => context.verifyClaim(RatableClaims.CanRate).orElse(
        Option.when(ratingForCategory.size != state.categories.size)(
          s"Rating must contain ${state.categories.size} categories."
        )
      ),
      (state, context) => state.rate(context.replicaId, ratingForCategory)
    )

def rateEvent(replicaId: ReplicaId, ratingForCategory: Map[Int, Int], password: String)(using crypt: Crypt) =
  ???
  /*
  withProofs(RatableClaims.CanRate) { proofs => 
    EventWithContext(
      RateEvent(ratingForCategory),
      RatableContext(replicaId, proofs)
    )
  }
  */
