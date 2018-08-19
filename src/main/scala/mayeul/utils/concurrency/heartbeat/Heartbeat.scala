package mayeul.utils.concurrency.heartbeat

import mayeul.utils.concurrency.DoEvery

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

/**
  * Periodically executes `todo`.
  * The timestamp must come from a call to System.currentTimeMillis and its value is ignored when 0.
  */
class Heartbeat(period: Duration, todo: => Unit)(implicit ec: ExecutionContext)
    extends DoEvery(period, todo)