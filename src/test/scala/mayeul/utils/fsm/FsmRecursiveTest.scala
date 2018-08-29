package mayeul.utils.fsm

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

class MyFsm2() extends FsmImpl[RecursiveState] {
  protected val initialTransition: Boolean = false
  val stateCompanion: FsmStateCompanion[RecursiveState] = RecursiveState

  def manualSwitching(to: RecursiveState): Unit = transitionTo(to)
}

abstract class RecursiveState extends FsmState

object RecursiveState extends FsmStateCompanion[RecursiveState] {
  case object A extends RecursiveState {
    val nextStates: Set[FsmState] = Set(Y)
  }

  case object W extends RecursiveState {
    val nextStates: Set[FsmState] = Set(A)
    override lazy val specializationOf = Some(X)
  }

  case object X extends RecursiveState {
    val nextStates: Set[FsmState] = Set(A)
    override lazy val specializationOf = Some(Y)
  }

  case object Y extends RecursiveState {
    val nextStates: Set[FsmState] = Set(A)
    override lazy val specializationOf = None
  }

  case object Z extends RecursiveState {
    val nextStates: Set[FsmState] = Set(A)
  }

  val initialState: RecursiveState = A
  val states = Seq(A, W, X, Y, Z)
}
