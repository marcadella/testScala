package mayeul.utils.runners

import mayeul.utils.runners.heartbeat.Watchdog
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent._
import scala.concurrent.duration._
import org.scalatest.concurrent.Waiters._

class WatchdogTest extends FunSpec with Matchers {
  implicit val ec = ExecutionContext.global
  describe(classOf[WatchdogTest].getName) {
    it("should execute the todo 1 time") {
      val w = new Waiter
      var i = 0
      var j = 0
      def todo(): Unit = {
        synchronized {
          i += 1
        }
        w.dismiss()
      }
      val initialTimestamp = System.currentTimeMillis()
      def timestamp: Long =
        if (j < 2) {
          j += 1
          System.currentTimeMillis()
        } else
          initialTimestamp

      val cr = new Watchdog(50.millis, 500.millis, timestamp, todo())
      w.await(timeout(600.millis)) //Used for thread sync
      Thread.sleep(800)
      synchronized { i } should be(1)
      synchronized { cr.isCancelled } should be(false)
      synchronized { cr.isCompleted } should be(true)
    }
  }
}
