package mayeul.utils.reactiveFsm

import scala.concurrent.{Future, Promise}
import scala.util.Try

/**
  * DirectedGraphDecorator decorates StateHolderLike when the state S contains a list of allowed other states to transition to
  * as well as the concept of terminal state (state which doesn't have any other next state than itself or none)
  * Note: Self transition are always allowed since a self transition would not trigger the observer
  *
  * @tparam S
  * @tparam SideEffectHandle
  */
trait DirectedGraphDecorator[S <: DGState, SideEffectHandle] {
  this: StateHolderLike[S, SideEffectHandle] =>

  /**
    * Registers side effect when reaching a terminal state
    * Attention: since we cannot guaranty that the callback will be performed before GC. Use finish With if you want to be sure the callback is executed
    */
  final def onTerminalState(sideEffect: S => Unit,
                            skipInitial: Boolean = false): SideEffectHandle =
    onStateChange(sideEffect, _.isTerminal, skipInitial)

  /**
    * Registers side effect on invalid transition
    * Note: This is a reactive state holder, it doesn't have any control over it's state, so it wouldn't make any sense to throw an exception in case the transition is not valid.
    * Instead use it for taking corrective action (or logging)
    */
  final def onInvalidTransition(
      sideEffect: (S, S) => Unit,
      skipInitial: Boolean = false): SideEffectHandle =
    onTransition(sideEffect,
                 (oldS, newS) => !oldS.canTransitionTo(newS),
                 skipInitial)

  private val promise = Promise[S]()

  /**
    * The future completes as soon as the FSM enters a terminal state (for the first time as it should be)
    */
  lazy val future: Future[S] = promise.future

  /**
    * We have no control over the callbacks and potentially we would like to execute a piece of code compulsorily before releasing the future (i.e. ultimately GC)
    * In this case implement your code in finishWith which will for sure execute just before the future is released (and then before GC)
    */
  def finishWith(): Unit = ()

  onTerminalState(s =>
    Try {
      finishWith()
      promise.success(s)
  })
}
