package functions

import functions.services.*

class LoggerMock(
  debugMode: Boolean = true
) extends LoggerServiceInterface:
  def log(message: String, level: LogLevel = LogLevel.Information) =
    if debugMode then
      println(s"[$level] $message")
