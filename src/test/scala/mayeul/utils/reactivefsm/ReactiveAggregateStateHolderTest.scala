package mayeul.utils.reactivefsm

import mayeul.utils.reactiveFsm.impl.{
  ReactAggregator,
  ReactFsm,
  ReactStateHolder
}
import org.scalatest.{FunSpec, Matchers}
import rx._

class ReactiveAggregateStateHolderTest extends FunSpec with Matchers {
  class ReactFsmInt0(implicit val ctx: Ctx.Owner) extends ReactFsm[Int] {
    protected val initialState = 0
  }

  class ReactFsmInt2(implicit val ctx: Ctx.Owner) extends ReactFsm[Int] {
    protected val initialState = 2
  }

  class AggregateFsmInt(val dependencyList: Seq[ReactStateHolder[Int]])(
      implicit val ctx: Ctx.Owner)
      extends ReactAggregator[Int] {
    override def aggregate(s1: Rx[Int], s2: Rx[Int]): Rx[Int] = Rx {
      s1() + s2()
    }
  }
  describe(classOf[ReactFsmTest].getName) {
    implicit val ctx: Ctx.Owner = Ctx.Owner.safe()
    var stateFollower = -1
    val fsm0 = new ReactFsmInt0()
    val fsm2 = new ReactFsmInt2()
    val agg = new AggregateFsmInt(Seq(fsm0, fsm2))
    agg.onStateChange(x => stateFollower = x)
    it("Should be at the initial state") {
      agg.state should be(2)
      stateFollower should be(2)
    }
    it("Should be at the state 7") {
      fsm0.transitionTo(5)
      agg.state should be(7)
      stateFollower should be(7)
    }
    it("Should be at the state 11") {
      fsm2.transitionTo(6)
      agg.state should be(11)
      stateFollower should be(11)
    }
    it(
      "Should still be at the state 11 (no update for fsm2) then 9 (fsn2 is still taken into account for the computation)") {
      fsm2.unregisterAll()
      fsm2.transitionTo(7)
      agg.state should be(11)
      stateFollower should be(11)
      fsm0.transitionTo(2)
      agg.state should be(9)
      stateFollower should be(9)
    }
  }
}

class ReactFsmInt2(implicit val ctx: Ctx.Owner) extends ReactFsm[Int] {
  protected val initialState = 2
}
