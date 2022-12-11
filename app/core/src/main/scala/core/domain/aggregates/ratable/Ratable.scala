package core.domain.aggregates.ratable

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.framework.{*, given}
import kofre.base.*
import kofre.datatypes.*
import kofre.syntax.*

case class Category(
  val title: LWW[String],

) derives DecomposeLattice, Bottom

object Category:
  def empty = Category(LWW.empty)

case class Rating(
  val ratingForCategory: Map[Int, LWW[Int]],

) derives DecomposeLattice, Bottom

object Rating:
  def empty = Rating(Map.empty)

case class Ratable(
  _title: LWW[String],
  val categories: Map[Int, Category],
  _ratings: Map[String, Rating],

) derives DecomposeLattice, Bottom:
  def title = _title.read.getOrElse("")

  def rate(ratingForCategory: Map[Int, Int], replicaID: String) =
    Ratable.empty.copy(
      _ratings = Map(
        replicaID -> Rating(
          ratingForCategory.map((k, v) => (k, LWW.apply(v, replicaID)))
        )
      )
    )

  def categoriesWithRating: Map[Int, (Category, Int)] = 
    if _ratings.size == 0 then
      return Map()

    categories
      .map((index, category) =>
        (
          index,
          (
            category,
            _ratings
              .map(_._2.ratingForCategory.getOrElse(index, LWW.empty[Int]))
              .filter(!_.isEmpty)
              .map(_.map(_.value).getOrElse(0))
              .sum / _ratings.size
          )
        )
      )
      .toMap

object Ratable:
  given JsonValueCodec[Ratable] = JsonCodecMaker.make

  def empty: Ratable = 
    Ratable(LWW.empty, Map.empty, Map.empty)

  def apply(title: String, categories: List[String], replicaID: String) =
    empty.copy(
      _title = LWW.apply(title, replicaID),
      categories = categories
        .zipWithIndex
        .map((title, index) => 
          (
            index, 
            Category(LWW.apply(title, replicaID))
          ))
        .toMap
    )
      