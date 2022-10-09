package webapp

import colibri.*
import colibri.router.*
import colibri.router.Router
import org.scalajs.dom.window
import outwatch.*
import rescala.default.*
import webapp.pages.debugpage.*
import webapp.pages.homepage.*
import webapp.pages.sharepage.*
import webapp.pages.ratepage.*
import webapp.services.*

object Routes:
  val fromPath: Path => Page =
    case Root                => HomePage()
    case Root / "share" / id => SharePage(id)
    case Root / "rate"  / id => RatePage(id)
    
    case Root / "debug"      => DebugPage()

  val toPath: Page => Path =
    case HomePage()    => Root / ""
    case SharePage(id) => Root / "share" / id
    case RatePage(id)  => Root / "rate"  / id

    case DebugPage()   => Root / "debug"
