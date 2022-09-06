package webapp

import webapp.services.*

trait Services:
  lazy val distributionConfig: DistributionConfig
  lazy val stateDistributionService: StateDistributionService
  lazy val stateProviderService: StateProviderService
