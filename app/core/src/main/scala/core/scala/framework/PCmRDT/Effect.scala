package core.framework.CmRDT

case class Effect[A, R](
  val verify:  (WithContext[A, R]) => Boolean,
  val advance: (WithContext[A, R]) => WithContext[A, R]
)

def verifyRoles[A, R](source: EventSource[R], roles: R*) =
  (state: WithContext[A, R]) => source.hasProofs(state.findRoles(roles: _*))

def mutateWithoutContext[A, R](f: A => A) =
  (state: WithContext[A, R]) => state.copy(inner = f(state.inner))
