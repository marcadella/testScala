package mayeul.utils.reactiveFsm.impl

import rx.{Ctx, Rx}

/**
  * An aggregator is a ReactStateHolder having for state the aggregation of the states of other state holders
  */
trait ReactAggregator[S] extends ReactStateHolder[S] {
  implicit val ctx: Ctx.Owner

  def dependencyList: Seq[ReactStateHolder[S]]

  /**
    * State aggregation function
    * Must be commutative and associative!
    */
  def aggregate(s1: S, s2: S): S

  override val _state = dependencyList.map { _.stateRx }.reduce[Rx[S]] {
    case (a: Rx[S], b: Rx[S]) => Rx { aggregate(a(), b()) }
  }
}
