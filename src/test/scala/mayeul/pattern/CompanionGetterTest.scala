package mayeul.pattern

import org.scalatest.{FunSpec, Matchers}

//Note: Companion doesn't necessarily refer to a Singleton companion class
//Note: This is actually the same pattern as GenericBuilder
//Note: It would work as well without trait A

class CompanionGetterTest extends FunSpec with Matchers {
  describe(classOf[CompanionGetterTest].getName) {
    it("should find the companion of A1") {
      new WithCompanion[A1].c should be(A1C)
    }
  }
}

class WithCompanion[T <: A: CompanionGetter]() {
  val c: ACompanion[T] = CompanionGetter[T].getCompanion
}

trait A

class A1 extends A
class A2 extends A

object A1C extends ACompanion[A1]
object A2C extends ACompanion[A2]

trait ACompanion[T <: A]

trait CompanionGetter[T <: A] {
  def getCompanion: ACompanion[T]
}

object CompanionGetter {
  def apply[T <: A: CompanionGetter]: CompanionGetter[T] =
    implicitly[CompanionGetter[T]]

  implicit val a1: CompanionGetter[A1] = {
    new CompanionGetter[A1] {
      def getCompanion: ACompanion[A1] = A1C
    }
  }
  implicit val a2: CompanionGetter[A2] = {
    new CompanionGetter[A2] {
      def getCompanion: ACompanion[A2] = A2C
    }
  }
}
