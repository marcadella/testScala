package mayeul.utils.fsm

trait FsmWrapper[S <: FsmState] extends FSM[S] {
  protected def fsm: FSM[S]

  override final lazy val quietInitialTransition: Boolean =
    fsm.quietInitialTransition
  override final lazy val quietSelfTransition: Boolean = fsm.quietSelfTransition
  override final lazy val forceAllowSelfTransition: Boolean =
    fsm.forceAllowSelfTransition
  override final lazy val ignoreAllWhenTerminal: Boolean =
    fsm.ignoreAllWhenTerminal

  final lazy val stateCompanion: FsmStateCompanion[S] = fsm.stateCompanion
  final def state: S = fsm.state
  final def transitionTo(newState: S, arg: Any = null): Unit =
    fsm.transitionTo(newState, arg)

  //Note, if you have special transitional side effects, pass them to the fsm as parameters
}
