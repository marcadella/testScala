package mayeul.utils.concurrency

/**
  * Similar as RunnerLike but is suppose to execute indefinitely and hence doesn't return any Future.
  * Note: can be mixed with CancellableLike
  */
trait DaemonLike {
  protected def autoStart: Boolean
  def start(): Unit

  final protected def initialize(): Unit = {
    if (autoStart)
      start()
  }
}
