package functions.services

import core.messages.common.*
import core.domain.aggregates.ratable.*
import core.domain.aggregates.analytic.*
//import functions.state.services.*
import scala.reflect.Selectable.*
import scala.scalajs.js

class StateProviderService(
  services: {
//    val aggregateRepositoryFactory: AggregateRepositoryFactoryInterface
  }, 
  context: js.Dynamic
)
