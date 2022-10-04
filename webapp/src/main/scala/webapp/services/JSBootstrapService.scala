package webapp.services

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

// helper to load app.css with tailwind content into scalajs
// https://github.com/fun-stack/example/blob/master/webapp/src/main/scala/example/webapp/LoadCss.scala
@js.native
@JSImport("src/main/css/app.css", JSImport.Namespace)
private object Css extends js.Object

class JSBootstrapService:
  // fancy scalajs magic to load css. Service needs to be not lazy
  Css
