package mayeul.utils.concurrency

/**
  * cancel() cancels an asynchronous task
  * `isCanceled` is true when a task has been canceled
  */
trait CancellableLike {
  def isCancelled: Boolean
  def cancel(): Unit
}
