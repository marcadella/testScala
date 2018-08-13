package mayeul.utils.runners

import scala.concurrent._

/**
  * Runs an action in parallel (using the ExecutionContext) which can be canceled.
  * `isCompleted` returns true when the task completed (normally or was cancelled).
  * `isCancelled` returns true when cancelled before the task completed normally.
  * The cleanUp() function is executed when the task completes (normally or via cancellation)
  */
class CancellableRunner(todo: => Unit, autoStart: Boolean)(
    implicit executionContext: ExecutionContext)
    extends Runner(todo, autoStart)
    with CancellableLike {
  def cancel(): Unit = ft.cancel(true)
  final def isCancelled: Boolean = ft.isCancelled
}
