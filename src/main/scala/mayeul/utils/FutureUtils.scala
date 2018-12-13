package mayeul.utils

import akka.actor.ActorSystem

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, TimeoutException}
import akka.pattern.after
import mayeul.utils.logging.Logging

import scala.collection.generic.CanBuildFrom
import scala.util.{Failure, Success, Try}
import scala.language.higherKinds
import scala.concurrent.blocking

object FutureUtils extends Logging {

  /**
    * Retries a Future up to 'maxRetries' separated by 'period' in case of failure until success or number of retries expired
    */
  def retry[T](
      todo: => Future[T],
      maxRetries: Int = 5,
      period: Duration = 10.seconds,
      verbose: Boolean = true)(implicit ec: ExecutionContext): Future[T] = {
    if (maxRetries < 0)
      todo
    else {
      todo.recoverWith {
        case e =>
          if (verbose)
            log.info(s"Retrying action due to:", e)
          blocking {
            Thread.sleep(period.toMillis)
            retry(todo, maxRetries - 1, period, verbose)
          }
      }
    }
  }

  /**
    * Convenience to convert Try to Future. Present in the scala library 2.12 as Future.fromTry
    */
  def fromTry[T](result: Try[T]): Future[T] = {
    result match {
      case Success(v) => Future.successful(v)
      case Failure(e) => Future.failed(e)
    }
  }

  /**
    * Sequence a Map[A, Future[B]] -> Future[Map[A, B]]
    */
  def sequenceMap[A, B](m: Map[A, Future[B]])(
      implicit ec: ExecutionContext): Future[Map[A, B]] = {
    Future.sequence(m map {
      case (a, fb) =>
        fb map { b =>
          (a, b)
        }
    }) map { _.toMap }
  }

  /**
    * Same as Future.sequence but only keep the successful futures.
    * If all the futures failed, then the result is Future(List())
    */
  def sequenceSuccessful[A, M[X] <: TraversableOnce[X]](in: M[Future[A]])(
      implicit cbf: CanBuildFrom[M[Future[A]], A, M[A]],
      executor: ExecutionContext): Future[M[A]] = {
    in.foldLeft(Future.successful(cbf(in))) { (fr, fa) ⇒
      (for (r ← fr; a ← fa) yield r += a) fallbackTo fr
    } map (_.result())
  }

  /**
    * Same as sequenceMap but only keep the successful futures.
    */
  def sequenceMapSuccessful[A, B](m: Map[A, Future[B]])(
      implicit ec: ExecutionContext): Future[Map[A, B]] = {
    sequenceSuccessful(m map {
      case (a, fb) =>
        fb map { b =>
          (a, b)
        }
    }) map { _.toMap }
  }

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
