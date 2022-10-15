package webapp.store.aggregates.ratable

import kofre.base.*
import kofre.datatypes.*
import kofre.syntax.*
import webapp.store.framework.{*, given}

/*
case class RatableInfo(
  val title: String,
  val categories: Map[Int, String]
)

case class Submission(
  val replicaID: String,
  val ratings: Map[Int, Int],
)

case class RatableView(
  val overallScoreAcc: Int,
  val ratingsAcc: Map[Int, Int],
  val submissionCount: Int
)

object RatableView:
  def apply(info: RatableInfo, submissions: Set[Submission]) =
    val ratingsAcc = submissions
      .flatMap(_.ratings)
      .groupBy(_._1)
      .map((k, v) => (k, v.map(_._2).sum))

    new RatableView(
      ratingsAcc.values.sum / ratingsAcc.size,
      ratingsAcc,
      submissions.size
    )

case class Ratable(
  val info: RatableInfo,
  val view: RatableView,
  val submissions: Set[Submission],
) extends Lattice[Ratable]:
  def merge(left: Ratable, right: Ratable): Ratable =
    // submissions without replicaid duplicates
    val submissions = left.submissions ++ right.submissions
      .filterNot(s => left.submissions.exists(_.replicaID == s.replicaID))
      
    Ratable(
      left.info,
      RatableView(info, submissions),
      submissions
    ) 
*/

case class Ratable() derives DecomposeLattice, Bottom
