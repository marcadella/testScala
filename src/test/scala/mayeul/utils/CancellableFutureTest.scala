package mayeul.utils

import scala.concurrent.duration._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent._
import scala.util.{Failure, Success}

class CancellableFutureTest extends FunSpec with Matchers {
  implicit val ec = ExecutionContext.global
  describe(classOf[CancellableFutureTest].getName) {
    val c = Cancellable({ Thread.sleep(500) }, 1 / 0)
    val f = c.future
    f onComplete {
      case Success(_) => it("should not be successful") { 0 should be(1) }
      case Failure(e: CancellationException) =>
        it("should be canceled") { 1 should be(1) }
      case Failure(_) => it("should not be failed") { 0 should be(1) }
    }
    c.cancel()
    Await.ready(f, 1000.millis)
  }
}
