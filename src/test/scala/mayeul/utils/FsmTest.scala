package mayeul.utils

import org.scalatest.{FunSpec, Matchers}

import scala.util.{Failure, Try}

class FsmTest extends FunSpec with Matchers {
  describe(classOf[FsmTest].getName) {
    var i: Int = 0
    val fsm = new MyFsm(false, () => i = i + 1)
    it("Should be at the initial state") {
      fsm.state should be(AbceState.A)
    }
    it("Should  allow A -> B") {
      fsm.manualSwitching(AbceState.B)
      fsm.state should be(AbceState.B)
    }
    it("should not allow transition B -> B") {
      val test = Try {
        fsm.manualSwitching(AbceState.B)
        false
      } match {
        case Failure(FsmTransitionFaultException(AbceState.B, AbceState.B)) =>
          true
        case _ => false
      }
      test should be(true)
    }
    it("should have triggered the side effect when were transitioned to B") {
      fsm.manualSwitching(AbceState.E)
      i should be(1)
      fsm.state should be(AbceState.E)
    }
    it("should not allow transition E -> B") {
      val error = Try {
        fsm.manualSwitching(AbceState.B)
        false
      } match {
        case Failure(FsmTransitionFaultException(AbceState.E, AbceState.B)) =>
          true
        case _ => false
      }
      error should be(true)
    }
    it("should be able to recognize state from string") {
      AbceState.fromString("E") should be(AbceState.E)
      val error = Try {
        AbceState.fromString("Blabla")
        false
      } match {
        case Failure(UnknownStateException("Blabla")) =>
          true
        case _ => false
      }
      error should be(true)
    }
  }
}

class MyFsm(protected val initialTransition: Boolean, action: () => Unit)
    extends FSMImpl[AbceState] {
  val stateCompanion: StateCompanion[AbceState] = AbceState

  def manualSwitching(to: AbceState): Unit = transitionTo(to)
  def isInMagicState: Boolean = state.isMagic

  override protected def onTransitionTo(newState: AbceState,
                                        ctx: Any = null): Unit =
    newState match {
      case AbceState.B => action()
      case _           => ()
    }
}

abstract class AbceState extends State {
  def isMagic: Boolean
}

object AbceState extends StateCompanion[AbceState] {
  case object A extends AbceState {
    val isMagic = false
    val nextStates: Set[State] = Set(A, B, C, E)
  }

  case object B extends AbceState {
    val isMagic = false
    val nextStates: Set[State] = Set(
      A,
      C,
      E
    )
  }

  case object C extends AbceState {
    val isMagic = false
    val nextStates: Set[State] = Set()
  }

  case object E extends AbceState {
    val isMagic = true
    val nextStates: Set[State] = Set()
  }

  lazy val initialState: AbceState = A
  lazy val states = Seq(A, B, C, E)
}
