package mayeul.utils.concurrency

import scala.concurrent.Future

/**
  * Executes some logic asynchronously when start() is called.
  * `isCompleted` is true after the task execution completed.
  */
trait RunnerLike[P, R] {
  def isCompleted: Boolean
  def execute(p: P): Future[R]
}
