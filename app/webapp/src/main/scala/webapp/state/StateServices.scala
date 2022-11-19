package webapp.state

import webapp.state.services.*

trait StateServices:
  lazy val applicationStateFactory: ApplicationStateFactory

