package mayeul.utils

import java.util.concurrent.{Callable, FutureTask}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

class Cancellable[T](executionContext: ExecutionContext, todo: => T) {
  private val promise = Promise[T]()

  def future: Future[T] = promise.future

  private val ft: FutureTask[T] = new FutureTask[T](
    new Callable[T] {
      override def call(): T = todo
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

object Cancellable {
  def apply[T](todo: => T)(
      implicit executionContext: ExecutionContext): Cancellable[T] =
    new Cancellable[T](executionContext, todo)
}
