package mayeul.utils.blocking

import mayeul.utils.logging.Logging

import scala.concurrent.duration._

case object MaxRetryException extends RuntimeException(s"Max retry reached")

/**
  * Performs an action and retries up to maxRetries times until action returns true (so in total the action will be attempted maxRetries + 1)
  * If maxRetries == 0 the action is not retried
  * When maxRetries < 0, RuntimeException is thrown
  */
class RetryUntilTrue(action: => Boolean, maxRetries: Int, period: Duration)
    extends Logging {
  if (maxRetries < 0)
    throw MaxRetryException

  if (!action) {
    log.info(s"Retrying action")
    Thread.sleep(period.toMillis)
    new RetryUntilTrue(action, maxRetries - 1, period)
  }
}

object RetryUntilTrue {
  def apply(action: => Boolean,
            maxRetries: Int = 10,
            period: Duration = 10.seconds) =
    new RetryUntilTrue(action, maxRetries, period)
}
