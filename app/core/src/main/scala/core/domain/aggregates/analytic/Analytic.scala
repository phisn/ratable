package core.domain.aggregates.analytic

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.framework.{*, given}
import kofre.base.*
import kofre.datatypes.*
import kofre.syntax.*

case class Visit(
  val userAgent: String,
  val referrer: String,
  val timestamp: String,
  val language: String,
)

case class Analytic(
  visits: Set[Visit]

) derives DecomposeLattice, Bottom

object Analytic:
  given JsonValueCodec[Analytic] = JsonCodecMaker.make

  def apply(visit: Visit): Analytic = 
    Analytic(Set(visit))
