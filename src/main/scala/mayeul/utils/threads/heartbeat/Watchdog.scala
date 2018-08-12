package mayeul.utils.threads.heartbeat

import mayeul.utils.threads.TimestampChecker

import scala.concurrent.duration.Duration

/**
  * Periodically checks a `timestamp` and executes once uponTimeoutDo() as soon as the timestamp timeouts.
  * The timestamp must come from a call to System.currentTimeMillis and its value is ignored when 0
  * To terminate within `todo` (when it is not related to a halt()):
  *   throw Terminate
  */
class Watchdog(
    period: Duration,
    timeout: Duration,
    lastHeartbeat: => Long,
    uponTimeoutDo: => Unit
) extends TimestampChecker(
      period,
      timeout,
      lastHeartbeat,
      overTimeoutDo = uponTimeoutDo,
      autoHalt = true
    ) {
  override protected val prefix: String = "watchdog"
}
