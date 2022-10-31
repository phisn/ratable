package webapp.services

enum LogLevel:
  case Trace, Debug, Info, Warning, Error, Critical, None

trait LoggerServiceInterface:
  def log(message: String, level: LogLevel = LogLevel.Info): Unit

  def error(message: String) = log(message, LogLevel.Error)
  def warning(message: String) = log(message, LogLevel.Warning)
  def trace(message: String) = log(message, LogLevel.Trace)

class LoggerService(services: {}, logLevel: LogLevel = LogLevel.Trace) extends LoggerServiceInterface:
  def log(message: String, logLevel: LogLevel) =
    if logLevel.ordinal >= this.logLevel.ordinal then
      println(s"[$logLevel] $message")
