package mayeul.utils

import scala.concurrent.duration._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent._
import scala.util.{Failure, Success}
import org.scalatest.concurrent.Waiters._

class CancellablePromiseTest extends FunSpec with Matchers {
  implicit val ec = ExecutionContext.global
  describe(classOf[CancellablePromiseTest].getName) {
    it("should be canceled") {
      val w = new Waiter
      val c = CancellablePromise({
        Thread.sleep(500)
      }, 1 / 0)
      val f = c.future
      f onComplete {
        case Success(_) =>
          w { assert(false) }
          w.dismiss()
        case Failure(e: CancellationException) =>
          w { assert(true) }
          w.dismiss()
        case Failure(_) =>
          w { assert(false) }
          w.dismiss()
      }
      c.cancel()
      w.await(timeout(600.millis))
    }
    it("should be failed") {
      val w = new Waiter
      val w2 = new Waiter
      val c = CancellablePromise({
        w2.dismiss()
      }, 1 / 0)
      val f = c.future
      f onComplete {
        case Success(_) =>
          w { assert(false) }
          w.dismiss()
        case Failure(e: CancellationException) =>
          w { assert(false) }
          w.dismiss()
        case Failure(_) =>
          w { assert(true) }
          w.dismiss()
      }
      w2.await(timeout(600.millis))
      Thread.sleep(200)
      c.cancel()
      w.await(timeout(600.millis))
    }
  }
}
