package functions.state

import functions.state.services.*

trait StateServices:
  lazy val aggregateRepositoryFactory: AggregateRepositoryFactoryInterface
