package mayeul.utils.fsm

trait FsmImpl[S <: FsmState] extends FSM[S] {
  private var _state: S = _

  private def isInit: Boolean = synchronized {
    _state == null
  }

  final def state: S = synchronized {
    if (isInit)
      _state = stateCompanion.initialState
    _state
  }

  final def transitionTo(newState: S, ctx: Any = null): Unit = {
    val init = isInit
    val oldState = state
    if (oldState.canTransitionTo(newState, forceAllowSelfTransition)) {
      if (oldState != newState) {
        synchronized {
          _state = newState
        }
      }
      if (oldState != newState || !quietSelfTransition) {
        if (!init || quietInitialTransition) {
          onTransitionToTerminal(newState, ctx)
          onTransitionTo(newState, ctx)
          onTransitionFromTo(oldState, newState, ctx)
        }
      }
    } else {
      if (!ignoreAllWhenTerminal || !oldState.isTerminal)
        throw FsmTransitionFaultException(oldState, newState)
    }
  }
}
