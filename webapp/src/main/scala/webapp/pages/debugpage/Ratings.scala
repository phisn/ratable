package webapp.pages.debugpage

import org.scalajs.dom
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import webapp.services.*
import webapp.store.aggregates.ratings.given
import webapp.store.aggregates.ratings.*
import webapp.store.framework.*
import webapp.Services
import webapp.given

def ratings(using services: Services) =
  div(
    services.stateProvider.ratings.map(ratings => 
      ratings
        .toList
        .sortBy((_, r) => r.value.read)
        .map(rating))
  )
