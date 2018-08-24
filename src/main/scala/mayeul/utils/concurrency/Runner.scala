package mayeul.utils.concurrency

import java.util.concurrent.{Callable, FutureTask}

import scala.concurrent._
import scala.util.Try

/**
  * Runs an action in parallel (using the ExecutionContext).
  * `isCompleted` returns true when the task completed.
  * The cleanUp() function is executed when the task completes
  */
class Runner[T](todo: => T)(implicit ec: ExecutionContext)
    extends RunnerLike[T] {
  private val promise = Promise[T]()

  protected val ft: FutureTask[T] = new FutureTask[T](
    new Callable[T] {
      override def call(): T = blocking {
        try {
          todo
        } catch {
          case e: Exception =>
            println(s"PRunner crashed", e)
            throw e
        }
      }
    }
  ) {
    override def done()
      : Unit = { //Executed when `todo` finishes normally or via cancellation
      try {
        promise.complete(Try(get()))
      } catch {
        case e: Exception =>
          promise.failure(e)
      }
    }
  }

  def execute(): Future[T] = {
    ec.execute(ft)
    promise.future
  }
  final def isCompleted: Boolean = ft.isDone
  protected def cleanUp(): Unit = ()
  //Executed once the task is completed (normally)
}

object Runner {
  def apply[T](todo: => T)(implicit ec: ExecutionContext) =
    new Runner[T](todo)(ec)
}

/**
  * Runs an action in parallel (using the ExecutionContext) which can be canceled.
  * `isCompleted` returns true when the task completed (normally or was cancelled).
  * `isCancelled` returns true when cancelled before the task completed normally.
  * The cleanUp() function is executed when the task completes (normally or via cancellation)
  */
class CancellableRunner[T](todo: => T, ec: ExecutionContext)
    extends Runner[T](todo)(ec)
    with CancellableLike {
  def cancel(): Unit = ft.cancel(true)
  final def isCancelled: Boolean = ft.isCancelled
}

object CancellableRunner {
  def apply[T](todo: => T)(implicit ec: ExecutionContext) =
    new CancellableRunner[T](todo, ec)
}
