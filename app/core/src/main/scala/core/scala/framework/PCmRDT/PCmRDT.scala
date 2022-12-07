package core.framework.CmRDT

case class WithContext[A, R](
  val inner: A,
  val roles: List[Role[R]]
):
  def isContextEmpty: Boolean = 
    roles.isEmpty

  def findRoles(ids: R*) =
    roles.filter(role => ids.contains(role.id)) 

// Permission enhanced Commutative Replicated Data Type
class PCmRDT[A, R](
  val state: WithContext[A, R]
)

/*

def main =
  val replicaId = "replicaId"

  // ! start
  val (counter, provers, createEvent) = CmRDT.create[Counter, CounterRoles]

  val eventSource = EventSource[CounterRoles](
    replicaId,
    proofs = provers.map(_.prove(replicaId))
  )

  val createEventWithSource = WithEventSource(
    createEvent,
    eventSource
  )
  
  // ? somehow distribute(createEventWithSource )

  // ! user wants to increment counter

  val addEvent = AddEvent(1)
  val addEffect = addEvent.asEffect(eventSource)

  // ... using effect

  if !addEffect.verify(counter.state) then
    throw Exception("Event is not valid")
  
  val newCounter = addEffect.advance(counter.state)

  // ... using event
  
  val addEventWithSource = WithEventSource(
    addEvent,
    eventSource
  )

  // ? somehow distribute(addEventWithSource)

*/
