package mayeul.utils.concurrency

import org.scalatest.{FunSpec, Matchers}
import org.scalatest.concurrent.Waiters._

import scala.concurrent._
import scala.concurrent.duration._

class CancellableDaemonTest extends FunSpec with Matchers {
  implicit val ec = ExecutionContext.global
  describe(classOf[CancellableDaemonTest].getName) {
    it("should execute the todo") {
      val w = new Waiter
      var i = 0
      var cleanupRan = false
      def todo(): Unit = {
        synchronized {
          i += 1
        }
        w.dismiss()
      }

      val cr = new CancellableDaemon(todo(), true, ec) {
        override def cleanUp(): Unit = {
          synchronized { cleanupRan = true }
        }
      }
      w.await(timeout(600.millis))
      Thread.sleep(200) //We leave enough time for the cleaning up to run
      synchronized { i } should be(1)
      synchronized { cr.isCancelled } should be(false)
      synchronized { cleanupRan } should be(true)
    }
    it("should execute the 1st part of todo") {
      val w = new Waiter
      var i = 0
      var cleanupRan = false
      def todo(): Unit = synchronized {
        i += 1
        w.dismiss()
        Thread.sleep(1000)
        i += 1
      }

      val cr = new CancellableDaemon(todo(), true, ec) {
        override def cleanUp(): Unit = {
          if (isCancelled)
            synchronized { cleanupRan = true }
        }
      }
      w.await(timeout(600.millis))
      cr.cancel()
      Thread.sleep(200) //We leave enough time for the cleaning up to run
      synchronized { i } should be(1)
      synchronized { cr.isCancelled } should be(true)
      synchronized { cleanupRan } should be(true)
    }
  }
}
