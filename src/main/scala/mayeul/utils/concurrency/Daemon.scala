package mayeul.utils.concurrency

import java.util.concurrent.{Callable, FutureTask}

import scala.concurrent._

/**
  * Runs an action in parallel (using the ExecutionContext).
  * `isCompleted` returns true when the task completed.
  * The cleanUp() function is executed when the task completes
  */
class Daemon(todo: => Unit, protected val autoStart: Boolean)(
    implicit ec: ExecutionContext)
    extends DaemonLike {
  protected val ft: FutureTask[Unit] = new FutureTask[Unit](
    new Callable[Unit] {
      override def call(): Unit = blocking {
        todo
        cleanUp()
      }
    }
  ) {
    override def done(): Unit =
      cleanUp() //Executed when `todo` finishes normally or via cancellation
  }

  def start(): Unit = ec.execute(ft)
  protected def cleanUp(): Unit = ()
  //Executed once the task is completed (normally)

  initialize()
}

object Daemon {
  def apply(todo: => Unit, autoStart: Boolean = true)(
      implicit ec: ExecutionContext) =
    new Daemon(todo, autoStart)(ec)
}

/**
  * Runs an action in parallel (using the ExecutionContext) which can be canceled.
  * `isCancelled` returns true when cancelled before the task completed normally.
  * The cleanUp() function is executed when the task completes via cancellation
  */
class CancellableDaemon(
    todo: => Unit,
    autoStart: Boolean,
    ec: ExecutionContext
) extends Daemon(todo, autoStart)(ec)
    with CancellableLike {
  def cancel(): Unit = ft.cancel(true)
  final def isCancelled: Boolean = ft.isCancelled
}

object CancellableDaemon {
  def apply(todo: => Unit, autoStart: Boolean = true)(
      implicit ec: ExecutionContext) =
    new CancellableDaemon(todo, autoStart, ec)
}
