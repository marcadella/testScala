package mayeul.utils.reactivefsm

import mayeul.utils.reactiveFsm.impl.ReactFsm
import org.scalatest.{FunSpec, Matchers}
import rx._

class ReactFsmTest extends FunSpec with Matchers {
  class ReactFsmInt2(implicit val ctx: Ctx.Owner) extends ReactFsm[Int] {
    protected val initialState = 2
  }
  describe(classOf[ReactFsmTest].getName) {
    implicit val ctx: Ctx.Owner = Ctx.Owner.safe()
    var stateFollower = -1
    var transitionFollower = -1
    val fsm = new ReactFsmInt2()
    fsm.onStateChange(x => stateFollower = x)
    it("Should be at the initial state") {
      fsm.state should be(2)
      stateFollower should be(2)
    }
    it("Should be at the state 5") {
      fsm.transitionTo(5)
      fsm.state should be(5)
      stateFollower should be(5)
    }
    it("Should be at the state 6 but the follower should still be at stage 5") {
      fsm.unregisterAll()
      fsm.transitionTo(6)
      fsm.state should be(6)
      stateFollower should be(5)
    }
    it("Should be at the state 7 and transitionFollower should be at 6*7") {
      val obs = fsm.onTransition((x, y) => transitionFollower = x * y)
      fsm.transitionTo(7)
      fsm.state should be(7)
      transitionFollower should be(42)
    }
    it("Should be at the state 3 and transitionFollower should be at 7*3") {
      //Note how even though the observer 'obs' is out of context it still is valid
      fsm.transitionTo(3)
      fsm.state should be(3)
      transitionFollower should be(21)
    }
    it("Should be followed by a Rx and properly handle Exceptions") {
      val c = Rx(6 / fsm.stateRx())
      //fsm.onStateChange(x => println(3 / x)) //We would get an exception when the callback is called at this line. Obs cannot deal with exceptions by itself: Try should be used inside not outside.
      fsm.transitionTo(6)
      fsm.state should be(6)
      c.now should be(1)
      fsm.transitionTo(0)
      fsm.state should be(0)
      c.toTry.isFailure should be(true)
    }
  }
}
