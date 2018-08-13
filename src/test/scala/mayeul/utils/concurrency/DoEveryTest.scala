package mayeul.utils.concurrency

import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import org.scalatest.concurrent.Waiters._

class DoEveryTest extends FunSpec with Matchers {
  implicit val ec = ExecutionContext.global
  describe(classOf[DoEveryTest].getName) {
    it("should execute the todo 4 times") {
      val w = new Waiter
      var i = 0

      def todo(): Unit = {
        synchronized {
          i += 1
        }
        w.dismiss()
      }

      val cr = new DoEvery(50.millis, todo())
      w.await(timeout(600.millis)) //Used for thread sync
      Thread.sleep(100)
      synchronized { cr.isCancelled } should be(false)
      Thread.sleep(90)
      cr.cancel()
      synchronized { i } should be(4)
      synchronized { cr.isCancelled } should be(true)
    }
    it("should execute the todo 3 times and not be cancelled") {
      val w = new Waiter
      var i = 0
      def todo(): Unit = {
        synchronized {
          i += 1
          if (i == 3)
            throw Terminate
        }
        w.dismiss()
      }

      val cr = new DoEvery(50.millis, todo())
      w.await(timeout(600.millis)) //Used for thread sync
      Thread.sleep(200)
      synchronized { i } should be(3)
      synchronized { cr.isCancelled } should be(false)
    }
  }
}
