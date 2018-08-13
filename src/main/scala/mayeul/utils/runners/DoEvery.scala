package mayeul.utils.runners

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

case object Terminate extends Exception

/**
  * Executes periodically `todo`
  * Because Thread.sleep() is used, `todo` should NOT check for Thread.interrupted()
  * To terminate internally (i.e. within `todo` and not with an external call to cancel()):
  *   throw Terminate
  */
class DoEvery(period: Duration, todo: => Unit, immediately: Boolean = true)(
    implicit ec: ExecutionContext)
    extends CancellableRunner(
      {
        if (!immediately)
          Thread.sleep(period.toMillis)
        try {
          while (true) { //We don't need to check Thread.interrupted() here because Thread.sleep() does it for us.
            todo //If the cancel is done here, the flag will be caught by the next Thread.sleep().
            Thread.sleep(period.toMillis)
          }
        } catch {
          case Terminate => //We just exit the loop
        }
      },
      true
    )
