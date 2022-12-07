package core.scala.framework.CmRDT

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
