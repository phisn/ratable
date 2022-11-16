package core.state

enum AggregateType:
  case Ratable

object AggregateType:
  given Conversion[AggregateType, String] = _.toString()
