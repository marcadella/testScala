package mayeul.utils.futureutils

import akka.actor.ActorSystem
import mayeul.utils.FutureUtils
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Success

class FutureUtilsTest extends FunSpec with Matchers {

  implicit val system = ActorSystem()
  implicit val ec = ExecutionContext.global

  describe(classOf[FutureUtilsTest].getName) {
    case class MyException() extends Exception
    implicit class MutableInt(var value: Int) {
      def inc(): Unit = { value += 1 }
    }
    val s = "succeded"
    val recover = "failed"
    def funcTest(se: MutableInt)(threshold: Int): String = {
      se.inc() //side effect
      if (se.value < threshold) {
        //println(s"Exception (${se.value} < $threshold)")
        throw MyException()
      } else {
        //println(s"OK: !(${se.value} < $threshold)")
        s
      }
    }
    describe("Under threshold") {
      val se = MutableInt(0)
      val f: Future[String] =
        FutureUtils.futureWithRetry(funcTest(se)(3), 100.millis, 6)
      val res = Await.result(f, Duration.Inf)
      it("2 retires") {
        assert(se.value === 3)
      }
      it(s"res should be $s") {
        assert(res === s)
      }
    }
    describe("Over threshold") {
      val se = MutableInt(0)
      val f: Future[String] =
        FutureUtils.futureWithRetry(funcTest(se)(10), 100.millis, 6)
      val res = try { Await.result(f, Duration.Inf) } catch {
        case _: Exception => recover
      }
      it("6 retries") {
        assert(se.value === 7)
      }
      it(s"res should be $recover") {
        assert(res === recover)
      }
    }
    describe("sequenceSuccessful should keep only successful futures") {
      val lf = Seq(Future(1), Future(2 / 0), Future(3))
      val fl = FutureUtils.sequenceSuccessful(lf)
      Await.ready(fl, 500.millis)
      fl.value should be(Some(Success(Seq(1, 3))))
    }
    describe("should retry the future 3 times") {
      var x = 2
      def f() = Future {
        x = x - 1
        if (x >= 0) {
          throw new Exception("blabla")
        } else
          x
      }
      Await.result(FutureUtils.retry(f(), period = 100.millis, verbose = false),
                   1.second) should be(-1)
    }
  }
}
