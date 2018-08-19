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
    it("Should be at B") {
      fsm.manualSwitching(AbceState.B)
      fsm.state should be(AbceState.B)
    }
    it("should not allow transition from B to B") {
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
    it("should not allow transition from E to B") {
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
        case Failure(UnknownState("Blabla")) =>
          true
        case _ => false
      }
      error should be(true)
    }
  }
}

class MyFsm(protected val initialTransition: Boolean, action: () => Unit)
    extends FSM[AbceState] {
  protected val stateCompanion: StateCompanion[AbceState] = AbceState

  def manualSwitching(to: AbceState): Unit = transitionTo(to)
  def isInMagicState: Boolean = state.isMagic

  override protected def onTransitionTo(newState: AbceState): Unit =
    newState match {
      case AbceState.B => action()
      case _           => ()
    }
}

abstract class AbceState extends State {
  def isMagic: Boolean
}

object AbceState extends StateCompanion[AbceState] {
  protected val tt = getTypeTag(this)
  case object A extends AbceState {
    val isMagic = false
    val nextStates = Seq(A, B, C, E)
  }

  case object B extends AbceState {
    val isMagic = false
    val nextStates = Seq(
      A,
      C,
      E
    )
  }

  case object C extends AbceState {
    val isMagic = false
    val nextStates = Seq()
  }

  case object E extends AbceState {
    val isMagic = true
    val nextStates = Seq()
  }

  val initialState: AbceState = A
}
