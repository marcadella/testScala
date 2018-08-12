package mayeul.utils.threads

import scala.concurrent.duration.Duration

case object Terminate extends Exception

/**
  * Executes periodically `todo`
  * Because Thread.sleep() is used, `todo` should NOT check for Thread.interrupted()
  * To terminate within `todo` (when it is not related to a halt()):
  *   throw Terminate
  */
class DoEvery(period: Duration, todo: => Unit, immediately: Boolean = true)
    extends HaltableRunner {
  override protected val prefix: String = "doEvery"
  protected def logic(): Unit = {
    if (!immediately)
      Thread.sleep(period.toMillis)
    try {
      while (true) { //We don't need to check Thread.interrupted() here because Thread.sleep() does it for us.
        todo //If the halt is done here, the flag will be caught by the next Thread.sleep().
        Thread.sleep(period.toMillis)
      }
    } catch {
      case Terminate => //We just exit the loop
    }
  }
}
