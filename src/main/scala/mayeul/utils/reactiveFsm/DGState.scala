package mayeul.utils.reactiveFsm

/**
  * Class modeling a state in a directed graph
  * canTransitionTo defines the space of next allowed states
  * Note that by default itself is not necessary an allowed state
  * isTerminal should be true iff whatever the next state ns other than itself, canTransitionTo(ns) = false
  */
abstract class DGState {
  def name: String
  def isTerminal: Boolean
  def canTransitionTo(otherState: DGState): Boolean
}
