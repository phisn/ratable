package webapp.state.services

import collection.immutable.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import kofre.base.*
import kofre.decompose.containers.*
import rescala.default.*
import rescala.operator.*
import scala.reflect.Selectable.*
import webapp.services.*
import webapp.state.framework.{*, given}
import webapp.state.{*, given}

class FacadeRepositoryFactory(services: {
  val logger: LoggerServiceInterface
  val facadeFactory: FacadeFactory
}):
  def registerAggregateAsRepository[A : JsonValueCodec : Bottom : Lattice](
    aggregateTypeId: String
  ): FacadeRepository[A] = 
    val facades = collection.mutable.Map[String, Facade[A]]()
  
    new FacadeRepository:
      // Creation of new aggregates is implicit. If an aggregate is requested that does not exist,
      // an empty aggregate is returned. This aggregate is then not saved until the first action is fired.
      def facade(id: String): Facade[A] =
        facades.getOrElseUpdate(
          id, 
          services.facadeFactory.registerAggregate(aggregateTypeId, id)
        )

      // Good question how to implement this one. :D
      def remove(id: String): Unit =
        ()
