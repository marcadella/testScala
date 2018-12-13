package mayeul.utils.retry

import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

class RetryOnFailureTest extends FunSpec with Matchers {
  implicit val ec = ExecutionContext.global
  describe(classOf[RetryOnFailure[_]].getName) {
    case object MyException extends Exception("Action failed")
    class C(i: Int) {
      var j = i
      def decr(): Int = {
        j = j - 1
        if (j > 0)
          throw MyException
        j
      }
    }
    it("should execute the action once") {
      val c = new C(-10)
      RetryOnFailure(c.decr(), 5, 100.millis, false)
      c.j should be(-11)
    }
    it("should execute the action until true") {
      val c = new C(2)
      RetryOnFailure(c.decr(), 5, 100.millis, false)
      c.j should be(0)
    }
    it("should execute the action until true (max tries)") {
      val c = new C(6)
      RetryOnFailure(c.decr(), 5, 100.millis, false)
      c.j should be(0)
    }
    it("should execute the action 6 times then fail") {
      val c = new C(10)
      Try {
        RetryOnFailure(c.decr(), 5, 100.millis, false)
      } match {
        case Success(_)           => assert(false)
        case Failure(MyException) => c.j should be(4)
        case _                    => assert(false)
      }
    }
    it("should execute the action 1 times then fail") {
      val c = new C(10)
      Try {
        RetryOnFailure(c.decr(), 0, 100.millis, false)
      } match {
        case Success(_)           => assert(false)
        case Failure(MyException) => c.j should be(9)
        case _                    => assert(false)
      }
    }
  }
}
