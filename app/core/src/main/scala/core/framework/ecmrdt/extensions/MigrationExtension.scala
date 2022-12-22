package core.framework.ecmrdt.extensions

import core.framework.*
import core.framework.ecmrdt.*
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global

trait MigrationsStateExtension[A]:
  def version: Int
  def migrate(version: Int): A

// Would it be possible to create a migration extension?
// The core problem that has to be solved is that we dont know when we get events. It
// might be the case, that we get events from older migrations

// So a migration extension would not only have to define a migration function from older state
// to newer state, but also a migration function from older event to newer event. This might not
// be viable.
