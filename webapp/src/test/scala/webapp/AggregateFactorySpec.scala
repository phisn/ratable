package webapp

import core.state.framework.{*, given}
import org.scalatest.*
import org.scalatest.flatspec.*
import org.scalatest.matchers.should.Matchers.*
import kofre.base.*
import webapp.aggregates.*
import webapp.mocks.*
import webapp.state.framework.*

class AggregateFactorySpec extends AnyFlatSpec
