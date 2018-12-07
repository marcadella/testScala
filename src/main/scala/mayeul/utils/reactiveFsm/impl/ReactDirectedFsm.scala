package mayeul.utils.reactiveFsm.impl

import mayeul.utils.reactiveFsm.{DGState, DirectedGraphDecorator}
import rx.Obs

import scala.util.Try

/**
  * Same as ReactFsm but with a DGState
  */
trait ReactDirectedFsm[S <: DGState]
    extends ReactFsm[S]
    with DirectedGraphDecorator[S, Obs] {

  case class WrongTransitionException(from: S, to: S)
      extends RuntimeException(
        s"Directed graph fault: transition ${from.name} -> ${to.name} does not exist")

  /**
    * Throws WrongTransitionException if the transition is not allowed
    */
  override def transitionTo(newState: S): Unit = {
    if (state.canTransitionTo(newState))
      super.transitionTo(newState)
    else
      throw WrongTransitionException(state, newState)
  }

  /**
    * Try transition to
    */
  def tryTransitionTo(newState: S): Unit = {
    Try {
      transitionTo(newState)
    } recover {
      case WrongTransitionException(s1, s2) =>
        log.warn(s"Could not perform transition ${s1.name} -> ${s2.name}")
      case e => throw e
    }
  }
}
