package mayeul.utils.runners

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

/**
  * Run periodically a `timestamp` check and executes underTimeoutDo() or overTimeoutDo() accordingly.
  * The timestamp must come from a call to System.currentTimeMillis and its value is ignored when 0
  * To terminate internally (i.e. within `todo` and not with an external call to cancel()):
  *   throw Terminate
  */
class TimestampChecker(
    period: Duration,
    timeout: Duration,
    timestamp: => Long,
    underTimeoutDo: => Unit = (),
    overTimeoutDo: => Unit = (),
    autoCancel: Boolean = false
)(implicit ec: ExecutionContext)
    extends DoEvery(
      period, {
        val t: Long = timestamp
        if (t > 0 && System.currentTimeMillis - t > timeout.toMillis) {
          overTimeoutDo
          if (autoCancel)
            throw Terminate
        } else {
          underTimeoutDo
        }
      }
    )
