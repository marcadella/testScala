package mayeul.utils.reactiveFsm

/**
  * StateHolderLike is a trait defining a container for a state having potentially the power to trigger side effects when the state is modified
  *
  * Note: Implementations should be thread safe
  *
  * @tparam S State type
  * @tparam SideEffectHandle Type of the handle to a newly added side effect (may be Unit)
  */
trait StateHolderLike[S, SideEffectHandle] {

  /**
    * Current state
    */
  def state: S

  /**
    * Register a side effect when state changes
    */
  def onStateChange(sideEffect: S => Unit,
                    withFilter: S => Boolean = _ => true,
                    skipInitial: Boolean = false): SideEffectHandle

  /**
    * Register a side effect when state changes with access to previous state
    */
  def onTransition(sideEffect: (S, S) => Unit,
                   withFilter: (S, S) => Boolean = (_, _) => true,
                   skipInitial: Boolean = false): SideEffectHandle
}
