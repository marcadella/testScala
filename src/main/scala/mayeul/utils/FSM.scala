package mayeul.utils

case class FsmTransitionFaultException(from: State, to: State)
    extends RuntimeException(
      s"FSM transition fault: transition ${from.name} -> ${to.name} does not exist")

case class UnknownStateException(s: String)
    extends RuntimeException(s"Unknown FSM state $s")

/**
  * State
  */
trait State extends Product with Serializable {
  lazy val name: String = productPrefix
  //This is a trick to get the class name without trailing '$'

  /**
    * If ThisState.isSpecializationOf == Some(SuperState), ThisState is a specialization (or sub-state) of state SuperState.
    * This means that: X.canTransitionTo(ThisState) if X.canTransitionTo(SuperState)
    * The side effects defined in the 'onTransition' methods may or may not follow the same rule.
    * Note: it is NOT true that ThisState.canTransitionTo(X) if SuperState.canTransitionTo(X)
    * Note: Can be recursive
    */
  lazy val specializationOf: Option[State] = None

  /**
    * A state is terminal if it has no following states
    */
  final lazy val isTerminal: Boolean = nextStates.isEmpty

  /**
    * List of the valid next states
    */
  protected def nextStates: Set[State]

  /**
    * True iff transition from this to nextState is valid
    */
  final def canTransitionTo(nextState: State): Boolean = {
    (nextState.specializationOf match {
      case Some(parent) if parent != nextState => //We remove infinite loop
        canTransitionTo(parent)
      case _ => false
    }) || (nextStates contains nextState)
  }
}

/**
  * To be extended as an object including all the state definitions
  */
abstract class StateCompanion[S <: State] extends Ordering[S] {
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

/**
  * A finite state machine
  * Note: that initially the state is null until a call to 'state' or 'transitionTo()' is performed.
  * So if it matters that the state is already set to the initial state at instance creation, add a dummy call to 'state'
  * in the body of the extending class.
  */
trait FSM[S <: State] {
  def stateCompanion: StateCompanion[S]
  def state: S

  /**
    * If set to false, the transition from "undefined state" to initial state will be considered as a proper
    * transition and side effects will be triggered.
    * If set to true, no side effect will be triggered.
    */
  def quietInitialTransition: Boolean = true

  /**
    * If true, if allowed, the transition from one state to itself yield side effects are muted.
    * Note: The self transition needs to be valid anyway (a state must appear in its nextState list).
    */
  def quietSelfTransition: Boolean = true

  /**
    * Transition to state 'newState' and trigger side effects.
    * Object 'arg' can be used to provide needed context to side effect functions.
    */
  def transitionTo(newState: S, arg: Any): Unit

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

trait FSMImpl[S <: State] extends FSM[S] {
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
    if (oldState.canTransitionTo(newState)) {
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
      //If both oldState and newState are terminal, we just ignore it. Otherwise we throw an exception
      if (!oldState.isTerminal || !newState.isTerminal)
        throw FsmTransitionFaultException(oldState, newState)
    }
  }
}

trait FSMWrapper[S <: State] extends FSM[S] {
  protected def fsm: FSM[S]

  override final val quietInitialTransition: Boolean =
    fsm.quietInitialTransition
  override final val quietSelfTransition: Boolean = fsm.quietSelfTransition

  val stateCompanion: StateCompanion[S] = fsm.stateCompanion
  def state: S = fsm.state
  def transitionTo(newState: S, arg: Any = null): Unit =
    fsm.transitionTo(newState, arg)

  //Note, if you have special transitional side effects, pass them to the fsm as parameters
}
