package mayeul.pattern

import org.scalatest.{FunSpec, Matchers}

//Note: This is actually the same pattern as CompanionFinder
//Note: It would work as well without trait B
//Note: It is probably easier to just specialize the class using the builder...

class GenericBuilderTest extends FunSpec with Matchers {
  describe(classOf[GenericBuilderTest].getName) {
    it("should build instance of B1") {
      new WithBuilder[B1].instance should be(B1())
    }
  }
}

class WithBuilder[T <: B: Builder]() {
  val instance: T = Builder[T].build()
}

trait B

trait Builder[T <: B] {
  def build(): T
}

case class B1() extends B
case class B2() extends B

object Builder {
  def apply[T <: B: Builder]: Builder[T] =
    implicitly[Builder[T]]

  implicit val b1: Builder[B1] = {
    new Builder[B1] {
      def build(): B1 = new B1
    }
  }
  implicit val b2: Builder[B2] = {
    new Builder[B2] {
      def build(): B2 = new B2
    }
  }
}
