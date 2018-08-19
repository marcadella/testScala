package mayeul.utils

import scala.reflect.runtime.{universe => ru}

case class FsmTransitionFaultException(from: State, to: State)
    extends RuntimeException(
      s"FSM transition fault: transition ${from.name} -> ${to.name} does not exist")

case class UnknownState(s: String)
    extends RuntimeException(s"Unknown FSM state $s")

/**
  * State
  */
trait State extends Product with Serializable {
  final val name: String = productPrefix
  //This is a trick to get the class name without trailing '$'

  /**
    * A state is terminal if it has no following states
    */
  final lazy val isTerminal: Boolean = nextStates.isEmpty

  /**
    * List of the valid next states
    */
  protected def nextStates: Seq[State]
  final def canTransitionTo(nextState: State): Boolean =
    nextStates contains nextState
}

/**
  * To be extended as an object including all the state definitions as CASE OBJECT (extending trait State)
  * In addition, copy paste the following line in the body:
  * protected val tt = getTypeTag(this)
  */
abstract class StateCompanion[S <: State: ru.TypeTag] {
  protected def tt: ru.TypeTag[_] //Must be extended as: 'getTypeTag(this)'
  protected def getTypeTag[T: ru.TypeTag](obj: T): ru.TypeTag[T] = ru.typeTag[T]

  final def states: Set[S] = {
    def isCaseObject(member: ru.Symbol): Boolean = {
      member.typeSignature match {
        case tpe if tpe <:< ru.typeOf[S] && tpe.getClass.isMemberClass =>
          true
        case _ => false
      }
    }
    (for {
      member <- tt.tpe.members
      if isCaseObject(member)
    } yield {
      reflect.runtime.currentMirror
        .reflectModule(member.asModule)
        .instance
        .asInstanceOf[S]
    }).toSet
  }

  final def fromString(s: String): S = {
    states find { state =>
      state.name == s
    } getOrElse { throw UnknownState(s) }
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

  /**
    * If set to true, the transition from "undefined state" to initial state will be considered as a proper
    * transition and side effects will be triggered.
    * If set to false, no side effect will be triggered.
    */
  protected def initialTransition: Boolean
  protected def stateCompanion: StateCompanion[S]
  private var _state: S = _

  private def isInit: Boolean = synchronized {
    _state == null
  }

  final def state: S = synchronized {
    if (isInit)
      _state = stateCompanion.initialState
    _state
  }

  final protected def transitionTo(
      newState: S,
      p: Boolean = false
  ): Unit = {
    val init = isInit
    val oldState = state
    if (oldState.canTransitionTo(newState)) {
      synchronized {
        _state = newState
      }
      if (!init || initialTransition) {
        onTransitionToTerminal(newState)
        onTransitionTo(newState)
        onTransitionFromTo(oldState, newState)
      }
    } else {
      if (!oldState.isTerminal || !newState.isTerminal) //In the opposite case we just ignore as that can happen but is meaningless
        throw FsmTransitionFaultException(oldState, newState)
    }
  }

  /**
    * The 3 methods below trigger a side effect conditionally depending on the new state, a property of the new state or {old -> new}
    * They are cumulative.
    */
  protected def onTransitionToTerminal(newState: S): Unit =
    if (newState.isTerminal) ()

  protected def onTransitionTo(newState: S): Unit = newState match {
    case _ => ()
  }

  protected def onTransitionFromTo(currentState: S, newState: S): Unit =
    (currentState, newState) match {
      case (_, _) => ()
    }
}
