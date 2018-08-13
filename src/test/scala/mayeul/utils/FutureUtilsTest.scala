package mayeul.utils

import akka.actor.ActorSystem
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class FutureUtilsTest extends FunSpec with Matchers {

  implicit val system = ActorSystem()

  describe(classOf[FutureUtilsTest].getName) {
    implicit val ec = ExecutionContext.global
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
  }
}
