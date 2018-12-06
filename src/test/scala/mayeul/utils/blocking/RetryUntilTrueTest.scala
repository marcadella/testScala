package mayeul.utils.blocking

import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class RetryUntilTrueTest extends FunSpec with Matchers {
  implicit val ec = ExecutionContext.global
  describe(classOf[RetryUntilTrue].getName) {
    class C(i: Int) {
      var j = i
      def decr(): Boolean = {
        j = j - 1
        j <= 0
      }
    }
    it("should execute the action once") {
      val c = new C(-10)
      RetryUntilTrue(c.decr(), 5, 100.millis, false)
      c.j should be(-11)
    }
    it("should execute the action until true") {
      val c = new C(2)
      RetryUntilTrue(c.decr(), 5, 100.millis, false)
      c.j should be(0)
    }
    it("should execute the action until true (max tries)") {
      val c = new C(6)
      RetryUntilTrue(c.decr(), 5, 100.millis, false)
      c.j should be(0)
    }
    it("should execute the action 6 times then fail") {
      val c = new C(10)
      Try {
        RetryUntilTrue(c.decr(), 5, 100.millis, false)
      } match {
        case Success(_)                 => assert(false)
        case Failure(MaxRetryException) => c.j should be(4)
        case Failure(_)                 => assert(false)
      }
    }
    it("should execute the action 1 times then fail") {
      val c = new C(10)
      Try {
        RetryUntilTrue(c.decr(), 0, 100.millis, false)
      } match {
        case Success(_)                 => assert(false)
        case Failure(MaxRetryException) => c.j should be(9)
        case Failure(_)                 => assert(false)
      }
    }
  }
}
