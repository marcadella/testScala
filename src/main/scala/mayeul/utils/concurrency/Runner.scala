package mayeul.utils.concurrency

import java.util.concurrent.{Callable, FutureTask}

import scala.concurrent._
import scala.util.Try

/**
  * Run an action in parallel (using the ExecutionContext).
  * `isCompleted` returns true when the task completed.
  * The cleanUp() function is executed when the task completes
  */
class Runner[P, R](todo: P => R)(implicit ec: ExecutionContext)
    extends RunnerLike[P, R] {
  private val promise = Promise[R]()
  private val param = Promise[P]()

  protected val ft: FutureTask[R] = new FutureTask[R](
    new Callable[R] {
      override def call(): R =
        blocking {
          //No need to try/catch because any Throwable will be caught by the Try block around 'get()'
          todo(param.future.value.get.get)
        } //In case of race condition, use Await
    }
  ) {
    override def done()
      : Unit = { //Executed when `todo` finishes normally or via cancellation
      Try {
        val res = get()
        promise.success(res)
      } recover {
        case e: Throwable =>
          promise.failure(e)
      }
      cleanUp()
    }
  }

  def execute(p: P): Future[R] = {
    param.success(p)
    ec.execute(ft)
    promise.future
  }
  final def isCompleted: Boolean = ft.isDone
  protected def cleanUp(): Unit = ()
  //Executed once the task is completed (normally)
}

object Runner {
  def apply[P, R](todo: P => R)(implicit ec: ExecutionContext) =
    new Runner[P, R](todo)(ec)
}

/**
  * Runs an action in parallel (using the ExecutionContext) which can be canceled.
  * `isCompleted` returns true when the task completed (normally or was cancelled).
  * `isCancelled` returns true when cancelled before the task completed normally.
  * The cleanUp() function is executed when the task completes (normally or via cancellation)
  */
class CancellableRunner[P, R](todo: P => R, ec: ExecutionContext)
    extends Runner[P, R](todo)(ec)
    with CancellableLike {
  def cancel(): Unit = ft.cancel(true)
  final def isCancelled: Boolean = ft.isCancelled
}

object CancellableRunner {
  def apply[P, R](todo: P => R)(implicit ec: ExecutionContext) =
    new CancellableRunner[P, R](todo, ec)
}
