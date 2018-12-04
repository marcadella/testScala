package mayeul.utils.fsm

/**
  * FSM State
  * Please, extend as an abstract class to prevent multiple inheritance
  */
trait FsmState extends Product with Serializable { //This is a trick to get the class name without trailing '$'
  lazy val name: String = productPrefix

  /**
    * If ThisState.isSpecializationOf == Some(SuperState), ThisState is a specialization (or sub-state) of state SuperState.
    * This means that: X.canTransitionTo(ThisState) if X.canTransitionTo(SuperState)
    * The side effects defined in the 'onTransition' methods may or may not follow the same rule.
    * Note: it is NOT true that ThisState.canTransitionTo(X) if SuperState.canTransitionTo(X)
    * Note: Can be recursive
    */
  lazy val specializationOf: Option[FsmState] = None

  /**
    * A state is terminal if it has no following states other than potentially himself
    */
  final lazy val isTerminal: Boolean = (nextStates - this).isEmpty

  /**
    * List of the valid next states
    * Note: by default self transitions are implied (so do not need to figure in nextStates).
    * If however FSM.forceAllowSelfTransition is set to false, then self transitions are not valid unless when this state is present in nextStates.
    */
  protected def nextStates: Set[FsmState]

  private def canTransitionToWithoutSpecialization(
      nextState: FsmState,
      forceAllowSelfTransition: Boolean): Boolean = {
    (nextStates contains nextState) || (forceAllowSelfTransition && (nextState == this))
  }

  /**
    * True iff transition from this to nextState is valid
    */
  final def canTransitionTo(
      nextState: FsmState,
      forceAllowSelfTransition: Boolean = false): Boolean = {
    (nextState.specializationOf match {
      case Some(parent)
          if parent != nextState => //We remove potential infinite loop
        canTransitionTo(parent) //Note: we don't propagate forceAllowSelfTransition here
      case _ => false
    }) || canTransitionToWithoutSpecialization(nextState,
                                               forceAllowSelfTransition)
  }
}

/**
  * To be extended as an object including all the state definitions
  */
abstract class FsmStateCompanion[S <: FsmState] extends Ordering[S] {
  //Attempt to use Reflexion failed: not always quick enough -> returns empty Set =(
  /**
    * List of all the states.
    * Note: We use a Seq to add ordering, so the order matters!
    */
  def states: Seq[S]

  final def compare(x: S, y: S): Int =
    states.indexOf(x) - states.indexOf(y)

  def fromString(s: String): S = {
    states find { state =>
      state.name == s
    } getOrElse { throw UnknownStateException(s) }
  }
  val initialState: S
}
