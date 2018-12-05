package mayeul.language.initialization

import org.scalatest.{FunSpec, Matchers}

/**
  * A val in a subclass is null/0 when used in the parent class
  * The rules of thumb are:
  *   - never have val on either side of an override unless the subclass defines the val in its constructor
  *   - since def can be overridden by val it is still possible to make the mistake, so just go for lazy val all the time!
  *   - Note that it is not enough to have a lazy val in the subclass: it must not be defined with any val inside!
  */
abstract class A {
  def a: Int = 1
  lazy val b: Int = 2
  val aCheck = a
  val bCheck = b //ok since b and bb are lazy val
  val c: Int //Ok since passed by parameter from B
}

class B(val c: Int) extends A {
  val aa = 10 //val = 0 in A
  lazy val bb = 20
  override lazy val a = aa //called in A so a takes value 0
  override lazy val b = bb
}

class LazyInitializationTest extends FunSpec with Matchers {
  val obj = new B(7)
  describe(classOf[LazyInitializationTest].getName) {
    it("should have wrong value for a and aCheck") {
      obj.a should be(0)
      obj.aCheck should be(0)
    }
    it("should have correct value for b and bCheck") {
      obj.b should be(20)
      obj.bCheck should be(20)
    }
    it("should have right value for c") {
      obj.c should be(7)
    }
  }
}
