package functions.services

import core.messages.common.*
import core.domain.aggregates.ratable.*
import functions.state.services.*
import scala.reflect.Selectable.*
import scala.scalajs.js

class StateProviderService(
  services: {
    val aggregateRepositoryFactory: AggregateRepositoryFactoryInterface
  }, 
  context: js.Dynamic
):
  lazy val ratables = services.aggregateRepositoryFactory.create[Ratable](AggregateType.Ratable)
