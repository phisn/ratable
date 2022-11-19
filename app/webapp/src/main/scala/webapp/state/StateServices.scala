package webapp.state

import webapp.state.services.*

trait StateServices:
  lazy val aggregateFactory: AggregateFactory
  lazy val applicationStateFactory: ApplicationStateFactory
  lazy val deltaDispatcher: DeltaDispatcherService
  lazy val facadeBootstrap: FacadeBootstrapService
  lazy val facadeFactory: FacadeFactory
