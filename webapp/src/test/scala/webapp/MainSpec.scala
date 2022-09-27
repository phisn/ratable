package webapp

import cats.effect.SyncIO
import org.scalajs.dom.*
import outwatch.*
import outwatch.dsl.*

class WebappSpec extends JSDomSpec {

  "You" should "probably add some tests" in {

    val message = "Hello World!"
    Outwatch.renderInto[SyncIO]("#app", h1(message)).unsafeRunSync()

    document.body.innerHTML.contains(message) shouldBe true
  }
}
