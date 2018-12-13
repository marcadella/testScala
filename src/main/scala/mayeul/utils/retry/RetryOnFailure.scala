package mayeul.utils.retry

import mayeul.utils.logging.Logging

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import scala.concurrent.blocking

/**
  * Performs an action and retries up to maxRetries times in case of failure (so in total the action will be attempted maxRetries + 1)
  * If maxRetries == 0 the action is not retried
  * When maxRetries is reached, the latest attempt error is thrown
  * Setting maxRetries < 0 throws InvalidParameterException
  */
class RetryOnFailure[A](action: => A,
                        maxRetries: Int,
                        period: Duration,
                        verbose: Boolean)
    extends Logging {
  private def start(): A = {
    assert(maxRetries >= 0)
    Try {
      action
    } match {
      case Success(res) => res
      case Failure(e) =>
        if (maxRetries > 0) {
          if (verbose)
            log.info(s"Retrying action due to:", e)
          blocking {
            Thread.sleep(period.toMillis)
            RetryOnFailure(action, maxRetries - 1, period, verbose)
          }
        } else
          throw e
    }
  }
}

object RetryOnFailure {
  def apply[A](action: => A,
               maxRetries: Int = 10,
               period: Duration = 10.seconds,
               verbose: Boolean = true): A =
    new RetryOnFailure(action, maxRetries, period, verbose).start()
}
