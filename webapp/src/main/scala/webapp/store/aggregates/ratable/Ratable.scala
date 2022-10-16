package webapp.store.aggregates.ratable

import kofre.base.*
import kofre.datatypes.*
import kofre.syntax.*
import webapp.store.framework.{*, given}

case class Category(
  val title: LWW[String] = LWW.empty,

) derives DecomposeLattice, Bottom

case class Rating(
  val ratingForCategory: Map[Int, LWW[Int]] = Map.empty,

) derives DecomposeLattice, Bottom

case class Ratable(
  val title: LWW[String] = LWW.empty,
  val categories: Map[Int, Category] = Map.empty,
  val ratings: Map[String, Rating] = Map.empty,

) derives DecomposeLattice, Bottom:
  def rate(ratingForCategory: Map[Int, Int], replicaID: String) =
    Ratable(ratings = Map(
      replicaID -> Rating(
        ratingForCategory.map((k, v) => (k, LWW.apply(v, replicaID)))
      )
    ))

  def categoriesWithRating: Map[Int, (Category, Int)] = 
    if ratings.size == 0 then
      return Map()

    categories
      .map((index, category) =>
        (
          index,
          (
            category,
            ratings
              .map(_._2.ratingForCategory.getOrElse(index, LWW.empty[Int]))
              .filter(!_.isEmpty)
              .map(_.map(_.value).getOrElse(0))
              .sum / ratings.size
          )
        )
      )
      .toMap
      
