package mayeul.utils.blocking

import java.security.InvalidParameterException

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

/**
  * Performs an action and retries up to maxRetries times in case of failure (so in total the action will be attempted maxRetries + 1)
  * If maxRetries == 0 the action is not retried
  * When maxRetries is reached, the latest attempt error is thrown
  * Setting maxRetries < 0 throws InvalidParameterException
  */
class RetryOnFailure[A](action: => A, maxRetries: Int, period: Duration) {
  private def start(): A = {
    if (maxRetries >= 0) {
      Try {
        action
      } match {
        case Success(res) => res
        case Failure(e) =>
          if (maxRetries > 0) {
            Thread.sleep(period.toMillis)
            RetryOnFailure(action, maxRetries - 1, period)
          } else
            throw e
      }
    } else {
      throw new InvalidParameterException("MaxRetry should be >= 0")
    }
  }
}

object RetryOnFailure {
  def apply[A](action: => A,
               maxRetries: Int = 10,
               period: Duration = 10.seconds): A =
    new RetryOnFailure(action, maxRetries, period).start()
}
