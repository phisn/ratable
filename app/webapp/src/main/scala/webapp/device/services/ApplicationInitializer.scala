package webapp.device.services

import org.scalajs.dom
import org.scalajs.dom.*
import rescala.default.*
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

// helper to load app.css with tailwind content into scalajs
// https://github.com/fun-stack/example/blob/master/webapp/src/main/scala/example/webapp/LoadCss.scala
@js.native
@JSImport("src/main/css/app.css", JSImport.Namespace)
private object Css extends js.Object

trait ApplicationInitializerInterface

class ApplicationInitializer(services: {}) extends ApplicationInitializerInterface:
  // Fancy scalajs magic to load css. Service needs to be not lazy
  Css
