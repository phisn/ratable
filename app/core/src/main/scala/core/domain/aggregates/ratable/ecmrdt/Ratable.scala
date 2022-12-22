package core.domain.aggregates.ratable.ecmrdt

import core.framework.*
import core.framework.ecmrdt.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

import core.framework.ecmrdt.extensions.{AsymPermissionContextExtension, AsymPermissionStateExtension, AsymPermissionEffectPipeline}
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

  val title: String,
  val categories: Map[Int, Category],
  val ratings: Map[String, Rating]
)
extends AsymPermissionStateExtension[RatableClaims]:
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

  def rate(replicaId: String, ratingForCategory: Map[Int, Int]): Ratable =
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

case class RatableContext(
  val replicaId: String,
  val proofs: Set[ClaimProof[RatableClaims]]
) 
extends IdentityContext 
   with AsymPermissionContextExtension[RatableClaims]

object Ratable:
  given (using Crypt): EffectPipeline[Ratable, RatableContext] = EffectPipeline(
    AsymPermissionEffectPipeline[Ratable, RatableClaims, RatableContext]
  )

trait RatableEvent extends Event[Ratable, RatableContext]

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

def rateEvent(replicaId: String, ratingForCategory: Map[Int, Int])(using registry: ClaimRegistry[RatableClaims]) =
  withProofs(RatableClaims.CanRate) { proofs => 
    EventWithContext(
      RateEvent(ratingForCategory),
      RatableContext(replicaId, proofs)
    )
  }
