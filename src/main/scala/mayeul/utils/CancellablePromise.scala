package mayeul.utils

import java.util.concurrent.{Callable, FutureTask}

import scala.concurrent._
import scala.util.Try

/**
  * Creates a Future[T] (vie `future`) which can be cancelled with cancel().
  * In case of cancellation, the Future is completed with a Failure(CancellationException).
  */
class CancellablePromise[T](todo: => T)(
    implicit executionContext: ExecutionContext) {
  private val promise = Promise[T]()

  def future: Future[T] = promise.future

  private val ft: FutureTask[T] = new FutureTask[T](
    new Callable[T] {
      override def call(): T = blocking { todo }
    }
  ) {
    override def done(): Unit = {
      try {
        val res = get()
        promise.complete(Try(res))
      } catch {
        case e: Exception =>
          promise.failure(e)
      }
    }
  }

  def cancel(): Unit = ft.cancel(true)

  executionContext.execute(ft)
}

object CancellablePromise {
  def apply[T](todo: => T)(
      implicit executionContext: ExecutionContext): CancellablePromise[T] =
    new CancellablePromise[T](todo)
}
