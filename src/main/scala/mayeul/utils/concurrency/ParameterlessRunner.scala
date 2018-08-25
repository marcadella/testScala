package mayeul.utils.concurrency

import java.util.concurrent.{Callable, FutureTask}

import scala.concurrent._
import scala.util.Try

/**
  * Runs an action in parallel (using the ExecutionContext).
  * `isCompleted` returns true when the task completed.
  * The cleanUp() function is executed when the task completes
  */
class ParameterlessRunner[R](todo: => R)(implicit ec: ExecutionContext)
    extends Runner[Unit, R]((Unit) => todo) {
  override def execute(p: Unit = ()): Future[R] = {
    super.execute(p)
  }
}

object ParameterlessRunner {
  def apply[R](todo: => R)(implicit ec: ExecutionContext) =
    new ParameterlessRunner[R](todo)(ec)
}

/**
  * Runs an action in parallel (using the ExecutionContext) which can be canceled.
  * `isCompleted` returns true when the task completed (normally or was cancelled).
  * `isCancelled` returns true when cancelled before the task completed normally.
  * The cleanUp() function is executed when the task completes (normally or via cancellation)
  */
class CancellableParameterlessRunner[R](todo: => R, ec: ExecutionContext)
    extends CancellableRunner[Unit, R]((Unit) => todo, ec)

object CancellableParameterlessRunner {
  def apply[R](todo: => R)(implicit ec: ExecutionContext) =
    new CancellableParameterlessRunner[R](todo, ec)
}
