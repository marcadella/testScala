package mayeul.language.initialization

import org.scalatest.{FunSpec, Matchers}

/**
  * An overridden val is properly resolved, BUT
  * another val using it would get 0 or null.
  * -> preferred method -> d
  */
trait A {
  val a: Int
  val aa = a * a

  def b: Int
  val bb = b * b

  def c: Int //Would work with val
  val cc = c * c

  def d: Int //Would work with val
  lazy val dd = d * d

  val e: Int = 10
  lazy val ee = e * e
}

class B extends A {
  val a = 2

  val b = 3

  lazy val c = 4

  val d = 5

  override val e = 6
}

class LazyInitializationTest extends FunSpec with Matchers {
  val obj = new B
  describe(classOf[LazyInitializationTest].getName) {
    it("should have wrong value for aa") {
      obj.a should be(2)
      obj.aa should be(0)
    }
    it("should have wrong value for bb") {
      obj.b should be(3)
      obj.bb should be(0)
    }
    it("should have right value for cc") {
      obj.c should be(4)
      obj.cc should be(16)
    }
    it("should have right value for dd") {
      obj.d should be(5)
      obj.dd should be(25)
    }
    it("should have right value for ee") {
      obj.e should be(6)
      obj.ee should be(36)
    }
  }
}
