package mayeul.utils.concurrency.heartbeat

import mayeul.utils.concurrency.TimestampChecker

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

/**
  * Periodically checks a `timestamp` and executes once uponTimeoutDo() as soon as the timestamp timeouts.
  * The timestamp must come from a call to System.currentTimeMillis and its value is ignored when 0
  * import Breaks.{break => terminate}
  *   Breaks.terminate()
  */
class Watchdog(
    period: Duration,
    timeout: Duration,
    timestamp: => Long,
    uponTimeoutDo: => Unit
)(implicit ec: ExecutionContext)
    extends TimestampChecker(
      period,
      timeout,
      timestamp,
      overTimeoutDo = uponTimeoutDo,
      autoCancel = true
    )
