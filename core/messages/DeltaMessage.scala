package core.messages

// Push upward to or downward from cloud 
// TODO: Specialize deltas to specific aggregates by aggregatae id
case class DeltaMessage(
  id: String,
  delta: String
)
