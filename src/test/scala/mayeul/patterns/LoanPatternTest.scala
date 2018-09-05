package mayeul.patterns

import java.io.Closeable

import org.scalatest.{FunSpec, Matchers}

/**
  * To close a resource automatically
  */
class LoanPatternTest extends FunSpec with Matchers {
  describe(classOf[LoanPatternTest].getName) {
    val resource = new C
    it("should close the resource after use") {
      resource.isClosed should be(false)
      LoanPattern.using(resource)(_ => ())
      resource.isClosed should be(true)
    }
  }
}

//With 'A <: { def close(): Unit }' we ask the compiler to check that A has a method close() : Unit
//We could as well use A <: Closeable
object LoanPattern {
  def using[A <: { def close(): Unit }, B](resource: A)(f: A => B): B =
    try {
      f(resource)
    } finally {
      resource.close()
    }
}

class C extends Closeable {
  var closed: Boolean = false

  def isClosed = synchronized(closed)

  def close(): Unit = {
    synchronized {
      closed = true
    }
  }
}
