package webapp

import colibri.*
import colibri.router.*
import colibri.router.Router
import org.scalajs.dom.window
import outwatch.*
import rescala.default.*
import webapp.application.pages.debugpage.*
import webapp.application.pages.homepage.*
import webapp.application.pages.sharepage.*
import webapp.application.pages.ratepage.*
import webapp.application.pages.viewpage.*
import webapp.services.*

object Routes:
  val fromPath: Path => Page =
    case Root                => HomePage()
    case Root / "share" / id => SharePage(id)
    case Root / "rate"  / id => RatePage(id)
    case Root / "view"  / id => ViewPage(id)
     
    case Root / "debug"      => DebugPage()

  val toPath: Page => Path =
    case HomePage()    => Root / ""
    case SharePage(id) => Root / "share" / id
    case RatePage(id)  => Root / "rate"  / id
    case ViewPage(id)  => Root / "view"  / id

    case DebugPage()   => Root / "debug"
