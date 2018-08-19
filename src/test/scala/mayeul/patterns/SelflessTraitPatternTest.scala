package mayeul.patterns

import org.scalatest.{FunSpec, Matchers}

//cf https://www.artima.com/scalazine/articles/selfless_trait_pattern.html

import Selfless.{v => value}

class SelflessTraitPatternTest extends FunSpec with Matchers {
  describe(classOf[SelflessTraitPatternTest].getName) {
    it("should find the companion of A1") {
      ExtensionOfSelfless.v should be(1)
      ExtensionOfOtherTrait.v should be("Hello")
      ExtensionOfOtherTrait.vImported should be(1)
    }
  }
}

object ExtensionOfSelfless extends Selfless

//Would not compile because of conflict with 'v'
//object ExtensionOfBoth extends Selfless with OtherTrait

object ExtensionOfOtherTrait extends OtherTrait {
  val vImported = value
}

trait Selfless {
  val v: Int = 1
}

object Selfless extends Selfless

trait OtherTrait {
  val v: String = "Hello"
}
