package core.state

case class AggregateId(
  val aggregateType: AggregateType,
  val id: String
)
