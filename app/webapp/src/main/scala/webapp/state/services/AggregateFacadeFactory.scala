package webapp.state.services

import com.github.plokhotnyuk.jsoniter_scala.core.*
import collection.immutable.*
import core.framework.*
import core.framework.ecmrdt.*
import core.messages.common.*
import core.messages.http.*
import core.messages.socket.*
import kofre.base.*
import rescala.default.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.Selectable.*
import scala.util.*
import webapp.services.*
import webapp.device.services.*
import webapp.state.framework.*

class AggregateFactory(services: {
  val logger: LoggerServiceInterface
  val functionsHttpApi: FunctionsHttpApiInterface
  val stateStorage: StateStorageService
}):
  def create[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec](gid: AggregateGid): Future[Option[AggregateFacade[A, C]]] =
    services.stateStorage.load[A, C](gid).map(_.map(initial =>
      val aggregate = AggregateFacade(initial)

      aggregate.listen.observe(aggregate =>
        services.stateStorage.save(gid, aggregate)
      )

      aggregate
    ))
