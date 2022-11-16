package webapp

import scala.concurrent.*

extension [A](f: Future[A])
  def now = f.value.get.get
