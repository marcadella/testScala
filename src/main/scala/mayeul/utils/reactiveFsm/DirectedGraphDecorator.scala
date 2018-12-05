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
    */
  def onTerminal(sideEffect: S => Unit): SideEffectHandle =
    onStateChange(sideEffect, _.isTerminal)

  /**
    * Registers side effect on invalid transition
    * Note: This is a reactive state holder, it doesn't have any control over it's state, so it wouldn't make any sense to throw an exception in case the transition is not valid.
    * Instead use it for taking corrective action (or logging)
    */
  def onInvalidTransition(sideEffect: (S, S) => Unit): SideEffectHandle =
    onTransition(sideEffect, (oldS, newS) => !oldS.canTransitionTo(newS), true)

  private val promise = Promise[S]()

  /**
    * The future completes as soon as the FSM enters a terminal state (for the first time as it should be)
    */
  lazy val toFuture: Future[S] = promise.future

  onTerminal(s =>
    Try {
      promise.success(s)
  })
}