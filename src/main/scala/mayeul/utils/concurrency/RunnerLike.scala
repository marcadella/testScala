package mayeul.utils.concurrency

import scala.concurrent.Future

/**
  * Executes some logic asynchronously when start() is called.
  * `isCompleted` is true after the task execution completed.
  */
trait RunnerLike[T] {
  def isCompleted: Boolean
  def execute(): Future[T]
}
