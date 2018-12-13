package mayeul.utils.retry

import mayeul.utils.logging.Logging

import scala.concurrent.duration._
import scala.concurrent.blocking

case object MaxRetryException extends RuntimeException(s"Max retry reached")

/**
  * Performs an action and retries up to maxRetries times until action returns true (so in total the action will be attempted maxRetries + 1)
  * If maxRetries == 0 the action is not retried
  * When maxRetries < 0, RuntimeException is thrown
  */
class RetryUntilTrue(action: => Boolean,
                     maxRetries: Int,
                     period: Duration,
                     verbose: Boolean)
    extends Logging {
  if (maxRetries < 0)
    throw MaxRetryException

  if (!action) {
    log.info(s"Retrying action")
    blocking {
      Thread.sleep(period.toMillis)
      new RetryUntilTrue(action, maxRetries - 1, period, verbose)
    }
  }
}

object RetryUntilTrue {
  def apply(action: => Boolean,
            maxRetries: Int = 10,
            period: Duration = 10.seconds,
            verbose: Boolean = true) =
    new RetryUntilTrue(action, maxRetries, period, verbose)
}
