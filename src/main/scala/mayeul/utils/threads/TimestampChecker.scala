package mayeul.utils.threads

import scala.concurrent.duration.Duration

/**
  * Run periodically a `timestamp` check and executes underTimeoutDo() or overTimeoutDo() accordingly.
  * The timestamp must come from a call to System.currentTimeMillis and its value is ignored when 0
  * To terminate within `todo` (when it is not related to a halt()):
  *   throw Terminate
  */
class TimestampChecker(
    period: Duration,
    timeout: Duration,
    timestamp: => Long,
    underTimeoutDo: => Unit = (),
    overTimeoutDo: => Unit = (),
    autoHalt: Boolean = false
) extends DoEvery(
      period, {
        val t: Long = timestamp
        if (t > 0 && System.currentTimeMillis - t > timeout.toMillis) {
          overTimeoutDo
          if (autoHalt)
            throw Terminate
        } else {
          underTimeoutDo
        }
      }
    ) {
  override protected val prefix: String = "timestampChecker"
}
