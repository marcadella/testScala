package mayeul.utils

import org.scalatest.{FunSpec, Matchers}

import scala.util.{Failure, Try}

class FsmRecursiveTest extends FunSpec with Matchers {
  describe(classOf[FsmTest].getName ++ "with recursive states") {
    val fsm = new MyFsm2()
    it("Should be at the initial state") {
      fsm.state should be(RecursiveState.A)
    }
    it("Should allow A -> W") {
      fsm.manualSwitching(RecursiveState.W)
      fsm.state should be(RecursiveState.W)
    }
    it("Should allow A -> X") {
      fsm.manualSwitching(RecursiveState.A)
      fsm.manualSwitching(RecursiveState.X)
      fsm.state should be(RecursiveState.X)
    }
    it("Should allow A -> F") {
      fsm.manualSwitching(RecursiveState.A)
      fsm.manualSwitching(RecursiveState.Y)
      fsm.state should be(RecursiveState.Y)
    }
    it("should not allow transition A -> Z") {
      fsm.manualSwitching(RecursiveState.A)
      val test = Try {
        fsm.manualSwitching(RecursiveState.Z)
        false
      } match {
        case Failure(
            FsmTransitionFaultException(RecursiveState.A, RecursiveState.Z)) =>
          true
        case _ => false
      }
      test should be(true)
    }
  }
}

class MyFsm2() extends FSM[RecursiveState] {
  protected val initialTransition: Boolean = false
  protected val stateCompanion: StateCompanion[RecursiveState] = RecursiveState

  def manualSwitching(to: RecursiveState): Unit = transitionTo(to)
}

abstract class RecursiveState extends State

object RecursiveState extends StateCompanion[RecursiveState] {
  //protected val tt = getTypeTag(this)

  case object A extends RecursiveState {
    val nextStates = Seq(Y)
  }

  case object W extends RecursiveState {
    val nextStates = Seq(A)
    override lazy val isSpecializationOf = Some(X)
  }

  case object X extends RecursiveState {
    val nextStates = Seq(A)
    override lazy val isSpecializationOf = Some(Y)
  }

  case object Y extends RecursiveState {
    val nextStates = Seq(A)
    override lazy val isSpecializationOf = None
  }

  case object Z extends RecursiveState {
    val nextStates = Seq(A)
  }

  val initialState: RecursiveState = A
  val states = Set(A, W, X, Y, Z)
}
