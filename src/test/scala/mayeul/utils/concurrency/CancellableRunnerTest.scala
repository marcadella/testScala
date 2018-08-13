package mayeul.utils.concurrency

import org.scalatest.concurrent.Waiters._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class CancellableRunnerTest extends FunSpec with Matchers {
  implicit val ec = ExecutionContext.global
  describe(classOf[CancellableRunnerTest].getName) {
    it("should be canceled") {
      val w = new Waiter
      val c = CancellableRunner({
        Thread.sleep(500)
        1 / 0
      })
      val f = c.execute()
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
      val c = CancellableRunner({
        w2.dismiss()
        1 / 0
      })
      val f = c.execute()
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
