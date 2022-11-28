package webapp.application.services

import rescala.default.*
import scala.reflect.Selectable.*
import webapp.application.*
import webapp.device.services.*
import webapp.services.ApplicationConfigInterface

class LocalizationService(services: {  
  val config: ApplicationConfigInterface
}):
  def get(key: String): Signal[String] =
    val entry = dictionary.getOrElse(key, Map(
      "en" -> s"<'$key' missing>"
    ))

    services.config.language.map(lang =>
      entry
        .find(key => lang.contains(key(0)))
        .getOrElse(entry.head)
        (1)
    )
