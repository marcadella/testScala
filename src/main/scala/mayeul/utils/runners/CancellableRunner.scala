package mayeul.utils.runners

import java.util.concurrent.{Callable, FutureTask}

import scala.concurrent._

/**
  * Runs an action in parallel (using the ExecutionContext) which can be canceled.
  * `isCompleted` returns true when the task completed (normally or was cancelled).
  * `isCancelled` returns true when cancelled before the task completed normally.
  * The cleanUp() function is executed when the task completes (normally or via cancellation)
  */
class CancellableRunner(todo: => Unit, protected val autoStart: Boolean)(
    implicit executionContext: ExecutionContext)
    extends RunnerLike(todo)
    with CancellableLike {
  private val ft: FutureTask[Unit] = new FutureTask[Unit](
    new Callable[Unit] {
      override def call(): Unit = blocking { todo }
    }
  ) {
    override def done(): Unit =
      cleanUp() //Executed when `todo` finishes normally or via cancellation
  }

  def start(): Unit = executionContext.execute(ft)
  def cancel(): Unit = ft.cancel(true)
  final def isCancelled: Boolean = ft.isCancelled
  final def isCompleted: Boolean = ft.isDone //normally or via cancellation
  protected def cleanUp(): Unit = ()
  //Executed once the task is completed (normally or via cancellation)

  initialize()
}
