package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import kofre.base.*
import kofre.decompose.containers.*
import rescala.default.*
import webapp.services.*
import webapp.state.framework.{*, given}
import webapp.state.{*, given}

import scala.reflect.Selectable.*


import scala.concurrent.ExecutionContext.Implicits.global
import org.scalajs.dom
import org.scalajs.dom.*
import scala.concurrent.*
import webapp.state.framework.*
import collection.immutable.*
import rescala.operator.*

class FacadeRepositoryFactory(services: {
  val logger: LoggerServiceInterface
  val facadeFactory: FacadeFactory
}):
  def registerAggregateAsRepository[A : JsonValueCodec : Bottom : Lattice](
    aggregateTypeId: String
  ): FacadeRepository[A] = 
    val facades = collection.mutable.Map[String, Facade[A]]()

    new FacadeRepository:
      def facade(id: String): Facade[A] =
        facades.getOrElseUpdate(
          id, 
          services.facadeFactory.registerAggregate(aggregateTypeId, id)
        )
