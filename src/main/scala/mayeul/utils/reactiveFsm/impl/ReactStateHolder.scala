package mayeul.utils.reactiveFsm.impl

import mayeul.utils.reactiveFsm.StateHolderLike
import rx.{Ctx, Obs, Rx}

/**
  * An implementation of a reactive state holder using ScalaRx
  * Reactive state holder means that one can register side effects when the state changes
  * Note that in this implementation self-transitions are allowed and do not trigger side effects.
  *
  * @tparam S
  */
trait ReactStateHolder[S] extends StateHolderLike[S, Obs] {
  implicit val ctx: Ctx.Owner //Use Ctx.Owner.safe()

  protected def _state: Rx[S]

  /**
    *   The current state
    */
  final def state: S = _state.now

  /**
    * State handle to create Rx.
    * Please do not use to register side effects. For that use the onXXX methods
    */
  final lazy val stateRx = _state

  private lazy val _transition: Rx[(S, S)] =
    _state.fold((_state.now, _state.now)) {
      case ((_, currS), newS) => (currS, newS)
    }

  /**
    * Register a side effect when state changes
    *
    * @param sideEffect
    * @param withFilter
    * @param skipInitial if set to true, the initialization doesn't trigger
    * @return
    */
  final def onStateChange(sideEffect: S => Unit,
                          withFilter: S => Boolean = _ => true,
                          skipInitial: Boolean = false): Obs = {
    def condAction(s: S): Unit = {
      if (withFilter(s))
        sideEffect(s)
    }
    if (skipInitial || !withFilter(_state.now))
      _state.triggerLater(condAction(_state.now))
    else
      _state.trigger(condAction(_state.now))
  }

  /**
    * Register a side effect when state changes with access to previous state
    *
    * @param sideEffect
    * @param withFilter
    * @param skipInitial
    * @return
    */
  final def onTransition(sideEffect: (S, S) => Unit,
                         withFilter: (S, S) => Boolean = (_, _) => true,
                         skipInitial: Boolean = false): Obs = {
    def condAction(s1: S, s2: S): Unit = {
      if (withFilter(s1, s2))
        sideEffect(s1, s2)
    }
    if (skipInitial)
      _transition.triggerLater({
        val tr = _transition.now
        condAction(tr._1, tr._2)
      })
    else
      _transition.trigger({
        val tr = _transition.now
        condAction(tr._1, tr._2)
      })
  }

  /**
    * Unregisters any previously registered side effect
    */
  final def unregisterAll(): Unit = _state.kill()
}
