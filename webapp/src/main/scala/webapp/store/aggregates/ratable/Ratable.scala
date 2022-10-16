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

  def plainRatings = 
    ratings
      .values
      .map(_.ratingForCategory.map((a, b) => (a, b.map(_.value).getOrElse(0)) ))
      .toList
