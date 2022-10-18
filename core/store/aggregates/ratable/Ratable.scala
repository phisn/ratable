package core.store.aggregates.ratable

import core.store.framework.{*, given}
import kofre.base.*
import kofre.datatypes.*
import kofre.syntax.*

case class Category(
  val title: LWW[String] = LWW.empty,

) derives DecomposeLattice, Bottom

case class Rating(
  val ratingForCategory: Map[Int, LWW[Int]] = Map.empty,

) derives DecomposeLattice, Bottom

case class Ratable(
  _title: LWW[String] = LWW.empty,
  val categories: Map[Int, Category] = Map.empty,
  _ratings: Map[String, Rating] = Map.empty,

) derives DecomposeLattice, Bottom:
  def title = _title.read.getOrElse("")

  def rate(ratingForCategory: Map[Int, Int], replicaID: String) =
    Ratable(_ratings = Map(
      replicaID -> Rating(
        ratingForCategory.map((k, v) => (k, LWW.apply(v, replicaID)))
      )
    ))

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
      