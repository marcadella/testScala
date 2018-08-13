package mayeul.utils.runners

import java.util.concurrent.{Callable, FutureTask}

import scala.concurrent._

/**
  * Runs an action in parallel (using the ExecutionContext).
  * `isCompleted` returns true when the task completed.
  * The cleanUp() function is executed when the task completes
  */
class Runner(todo: => Unit, protected val autoStart: Boolean)(
    implicit executionContext: ExecutionContext)
    extends RunnerLike {
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

  def start(): Unit = executionContext.execute(ft)
  final def isCompleted: Boolean = ft.isDone //normally
  protected def cleanUp(): Unit = ()
  //Executed once the task is completed (normally)

  initialize()
}
