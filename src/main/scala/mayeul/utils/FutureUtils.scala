package mayeul.utils

import akka.actor.ActorSystem

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, TimeoutException}
import akka.pattern.after

object FutureUtils {

  /**
    * Add the concept of timeout to a future
    *
    * Usage:
    * f withTimeout(5.seconds) onComplete {
    * case Success(x) => println(x)
    * case Failure(error) => println(error)
    * }
    */
  implicit class FutureExtensions[T](f: Future[T])(
      implicit ec: ExecutionContext) {
    def withTimeout(
        timeout: FiniteDuration = 10.seconds,
        timeoutException: => Throwable = new TimeoutException(
          "Future timed out"))(implicit system: ActorSystem): Future[T] = {
      if (timeout == 0.second)
        f
      else
        Future firstCompletedOf Seq(
          f,
          after(timeout, system.scheduler)(Future.failed(timeoutException)))
    }
  }

  /**
    * Create a future which will be retried at most 'maxRetries' times with 'wait' delay after each failure
    * until the future is successful (or the last failed future is returned)
    * If 'timeout' is > 0 then each attempt is bounded by the timeout.
    */
  def futureWithRetry[T](body: => T,
                         wait: FiniteDuration = 1.second,
                         maxRetries: Int = 5,
                         timeout: FiniteDuration = 0.second)(
      implicit system: ActorSystem,
      ec: ExecutionContext): Future[T] = {
    val future = Future { body } withTimeout timeout
    if (maxRetries > 0)
      future recoverWith {
        case _: Exception =>
          after(wait, system.scheduler)(
            futureWithRetry(body, wait, maxRetries - 1, timeout))
      } else
      future
  }
}
