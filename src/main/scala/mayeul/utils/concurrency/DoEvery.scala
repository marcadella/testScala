package mayeul.utils.concurrency

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

/**
  * Executes periodically `todo`
  * Because Thread.sleep() is used, `todo` should NOT check for Thread.interrupted()
  * To terminate internally (i.e. within `todo` and not with an external call to cancel()) call:
  * import Breaks.{break => terminate}
  *   Breaks.terminate()
  */
class DoEvery(period: Duration, todo: => Unit, immediately: Boolean = true)(
    implicit ec: ExecutionContext)
    extends CancellableDaemon(
      {
        import scala.util.control.Breaks.breakable
        if (!immediately)
          Thread.sleep(period.toMillis)
        breakable {
          while (true) { //We don't need to check Thread.interrupted() here because Thread.sleep() does it for us.
            todo //If the cancel is done here, the flag will be caught by the next Thread.sleep().
            Thread.sleep(period.toMillis)
          }
        }
      },
      true,
      ec
    )
