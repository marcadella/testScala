package mayeul.utils.reactiveFsm.impl

import rx.Var

/**
  * A reactive FSM implementation
  * @tparam S
  */
trait ReactFsm[S] extends ReactStateHolder[S] {

  /**
    * Initial state
    */
  protected def initialState: S

  protected lazy val _state: Var[S] = Var(initialState)

  /**
    * Ask the FSM to switch state potentially Throwing errors if it was not allowed
    */
  def transitionTo(newState: S): Unit = {
    _state() = newState
  }
}
