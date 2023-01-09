package core.framework

case class RatableError(
  val messages: List[Map[String, String]]
):
  def default: String = 
    messages.map(x => x.getOrElse("en", x.head)).mkString(", ")
  
  def combine(other: RatableError): RatableError =
    RatableError(messages ++ other.messages)

object RatableError:
  def apply(): RatableError =
    new RatableError(List())

  def apply(message: String): RatableError =
    new RatableError(List(Map("en" -> message)))
  
  def apply(message: Map[String, String]): RatableError =
    new RatableError(List(message))

  def apply(messages: List[Map[String, String]]): RatableError =
    new RatableError(messages)
