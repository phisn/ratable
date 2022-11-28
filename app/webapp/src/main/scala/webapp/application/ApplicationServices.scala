package webapp.application

import webapp.*
import webapp.application.framework.given
import webapp.application.services.*

trait ApplicationServices:
  lazy val popup: PopupService
  lazy val local: LocalizationService
  lazy val routing: RoutingService

type ServicesWithApplication = Services with ApplicationServices
