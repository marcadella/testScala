package mayeul.utils.fsm

case class FsmTransitionFaultException(from: FsmState, to: FsmState)
    extends RuntimeException(
      s"FSM transition fault: transition ${from.name} -> ${to.name} does not exist")

case class UnknownStateException(s: String)
    extends RuntimeException(s"Unknown FSM state $s")

/**
  * A finite state machine
  * Note: that initially the state is null until a call to 'state' or 'transitionTo()' is performed.
  * So if it matters that the state is already set to the initial state at instance creation, add a dummy call to 'state'
  * in the body of the extending class.
  */
trait FSM[S <: FsmState] {
  def stateCompanion: FsmStateCompanion[S]
  def state: S

  /**
    * If set to false, the transition from "undefined state" to initial state will be considered as a proper
    * transition and side effects will be triggered.
    * If set to true, no side effect will be triggered.
    */
  lazy val quietInitialTransition: Boolean = true

  /**
    * If true, if allowed, the transition from one state to itself yield side effects are muted.
    * Note: The self transition needs to be valid anyway (a state must appear in its nextState list).
    */
  lazy val quietSelfTransition: Boolean = true

  /**
    * Self transitions are usually defined within the state definition itself, but one can override it here.
    * If true, self transition are allowed for all the states even though the states are not defined as self-transitoire
    * If false, it is the state definition that rules.
    */
  lazy val forceAllowSelfTransition: Boolean = true

  /**
    * If set to true, any transition is silently ignored (transitionTo has no effect) after a terminal state has been reached
    * (which would have been by definition an invalid transition).
    */
  lazy val ignoreAllWhenTerminal: Boolean = true

  /**
    * Transition to state 'newState' and trigger side effects.
    * Object 'ctx' can be used to provide needed context to side effect functions.
    */
  def transitionTo(newState: S, ctx: Any): Unit

  /**
    * The 3 methods below trigger a side effect conditionally depending on the new state, a property of the new state or {old -> new}
    * They are cumulative.
    */
  protected def onTransitionToTerminal(newState: S, ctx: Any): Unit =
    if (newState.isTerminal) ()

  protected def onTransitionTo(newState: S, ctx: Any): Unit = newState match {
    case _ => ()
  }

  protected def onTransitionFromTo(currentState: S,
                                   newState: S,
                                   ctx: Any): Unit =
    (currentState, newState) match {
      case (_, _) => ()
    }
}
