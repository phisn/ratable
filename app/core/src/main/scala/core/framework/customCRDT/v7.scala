package core.framework.customCRDT.v7

case class Effect[A, R](
  val verify:  A => Boolean,
  val advance: A => A
)

trait Event[A, C]:
  def asEffect(context: C): Effect[A, C]

// Context Features
trait ContextFeatureReplicaId:
  val replicaId: String

class CmRDt[A](
  val state: A,
)

class Role[R](
  val id: R,
  val publicKey: Array[Byte]
)

class RoleProof[R](
  val id: R,
  val proof: Array[Byte]
)
  
trait StateFeaturePermission[R]:
  val roles: List[Role[R]]
  val assignments: Map[String, List[R]]

trait PermissionEvent

case class AssignRoleEvent[A <: StateFeaturePermission[R], C, R](
  val replicaId: String,
  val proofs: List[RoleProof[R]],

) extends Event[A, C], PermissionEvent:
  def asEffect(context: C): Effect[A, C] =
    Effect(
      (state: A) => 
      (state: A) => state
    )

def newPermissionCmRDT =
  ???

  