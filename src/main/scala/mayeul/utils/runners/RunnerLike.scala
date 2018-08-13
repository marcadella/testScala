package mayeul.utils.runners

/**
  * Executes logic `todo` asynchronously when start() is called.
  * If `autoStart` is set to true, the execution starts straight after the instance creation.
  * `isCompleted` is true after the task execution completed.
  */
abstract class RunnerLike(todo: => Unit) {
  def isCompleted: Boolean
  protected def autoStart: Boolean
  def start(): Unit

  final protected def initialize(): Unit = {
    if (autoStart)
      start()
  }
}

/**
  * cancel() cancels an asynchronous task
  * `isCanceled` is true when a task has been canceled
  */
trait CancellableLike {
  def isCancelled: Boolean
  def cancel(): Unit
}
