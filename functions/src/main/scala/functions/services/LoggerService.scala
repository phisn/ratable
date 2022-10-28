package functions.services

import scala.reflect.Selectable.*
import scala.scalajs.js

enum LogLevel:
  case Trace, Debug, Info, Warning, Error, Critical, None

trait LoggerServiceInterface:
  def log(message: String, level: LogLevel = LogLevel.Info): Unit

  def error(message: String) = log(message, LogLevel.Error)
  def warning(message: String) = log(message, LogLevel.Warning)
  def trace(message: String) = log(message, LogLevel.Trace)

class LoggerService(
  services: {}, 
  context: js.Dynamic,
  logLevel: LogLevel = LogLevel.Trace
) extends LoggerServiceInterface:
  def log(message: String, logLevel: LogLevel) =
    if logLevel.ordinal >= this.logLevel.ordinal then
      context.log(s"[$logLevel] $message")
