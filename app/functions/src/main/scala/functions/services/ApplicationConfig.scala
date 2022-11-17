package functions.services

import scala.reflect.Selectable.*
import scala.scalajs.js

class ApplicationConfig(
  services: {}, 
  context: js.Dynamic
):
  def cosmosDBConnectionString = js.Dynamic.global.process.env.CosmosDBConnectionString.asInstanceOf[String]
